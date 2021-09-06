/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shulie.surge.data;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.shulie.surge.data.common.utils.DateUtils;
import io.shulie.surge.data.common.utils.IpAddressUtils;
import io.shulie.surge.data.runtime.disruptor.EventFactory;
import io.shulie.surge.data.runtime.disruptor.EventHandler;
import io.shulie.surge.data.runtime.disruptor.RingBuffer;
import io.shulie.surge.data.runtime.disruptor.dsl.Disruptor;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;

import static io.shulie.surge.data.common.utils.CommonUtils.defaultIfNull;
import static io.shulie.surge.data.common.utils.CommonUtils.trimToNull;

/**
 * @author pamirs
 */
public class RollingHDFSSupport implements HDFSSupport {
    private static final Logger logger = LoggerFactory.getLogger(RollingHDFSSupport.class);

    private static final int RING_BUFFER_SIZE = 1024;
    //文件写入心跳时间间隔
    private static final long FILE_HEART_BEAT_TIME = 25000;
    private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd/HH_";
    private static final String EXTENTION = ".seq";
    private static final String LOCAL_IP_STR = IpAddressUtils.getLocalAddress().replace(".", "");
    private static final CompressionCodec DEFAULT_CODEC = new DefaultCodec();
    private CompressionCodec codec = DEFAULT_CODEC;

    private final Disruptor<SerialWritingJob> disruptor;
    private final RingBuffer<SerialWritingJob> ringBuffer;

    private final String staticPath;
    private final String fileName;
    private final String uniqId;
    private final SimpleDateFormat dateFormatter;

    private final ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService es;

    private final Object eofLock = new Object();

    private static class WriterHandler {
        SequenceFile.Writer writer;
        Path path;
        long expireTime;
        boolean closed = true;
        long lastModifyTime = System.currentTimeMillis();
    }

    private static class SerialWritingJob {
        JobType type = JobType.DEFAULT;
        Writable key;
        Writable value;
        Runnable errCallback;
        String dynamicPath;
        long timestamp;
        public final static EventFactory<SerialWritingJob> EVENT_FACTORY = new EventFactory<SerialWritingJob>() {
            @Override
            public SerialWritingJob newInstance() {
                return new SerialWritingJob();
            }
        };
    }

    private static enum JobType {
        DEFAULT,
        END_OF_WORLD,
        EXPIRE,
        HEART_BEAT
    }

    @SuppressWarnings("unchecked")
    public RollingHDFSSupport(final String filePath, final String fileName, String dateFormat, final String uniqId) {
        this.fileName = fileName;
        this.staticPath = filePath;
        this.uniqId = uniqId;
        dateFormat = defaultIfNull(trimToNull(dateFormat), DEFAULT_DATE_FORMAT);
        this.dateFormatter = new SimpleDateFormat(dateFormat);
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat(
                "HDFS Writer:" + filePath + "-thread-%d").build();
        es = Executors.newCachedThreadPool(namedThreadFactory);
        this.disruptor = new Disruptor<>(SerialWritingJob.EVENT_FACTORY, RING_BUFFER_SIZE, es);
        this.disruptor.handleEventsWith(new SerialWritingHandler());
        this.ringBuffer = disruptor.start();
        schedule.scheduleAtFixedRate(new Runnable() {
            private int count;
            @Override
            public void run() {
                //一秒钟发一个心跳包，一分钟发一个超时检测包
                //publishEvent(JobType.HEART_BEAT);
                if (count++ >= 60) {
                    publishEvent(JobType.EXPIRE);
                    count = 0;
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void write(final String key, final String value, final long timestamp, final Runnable errCallback,
                      final String dynamicPath) {
        long seq = ringBuffer.next();
        SerialWritingJob job = ringBuffer.get(seq);
        job.key = new Text(key);
        job.value = new Text(value);
        job.timestamp = timestamp;
        job.errCallback = errCallback;
        job.dynamicPath = dynamicPath;
        job.type = JobType.DEFAULT;
        ringBuffer.publish(seq);
    }

    @Override
    public void stop() throws Exception {
        publishEvent(JobType.END_OF_WORLD);
        logger.warn("End of World: HDFS closing hook has been invoked.");
        synchronized (eofLock) {
            eofLock.wait();
        }
        disruptor.shutdown();
        es.shutdown();
        schedule.shutdown();
        logger.warn("End of World: HDFS closing hook has been completed.");
    }

    private void publishEvent(final JobType jobType) {
        long seq = ringBuffer.next();
        SerialWritingJob job = ringBuffer.get(seq);
        job.type = jobType;
        ringBuffer.publish(seq);
    }

    class SerialWritingHandler implements EventHandler<SerialWritingJob> {
        private final Map<String, WriterHandler> datePathWriterMap = new HashMap<>(64);
        private final Configuration conf = buildConfig();
        private final int WRITER_FLUSH_SIZE = 50000;
        private int flushCount = 0;
        private long count = 0;

        @Override
        public void onEvent(final SerialWritingJob event, final long sequence, final boolean endOfBatch){
            WriterHandler writerHandler = null;
            String pathKey = null;
            try {
                if (event.type != JobType.DEFAULT) {
                    if (event.type == JobType.END_OF_WORLD) {
                        logger.warn("End of World: closing file...");
                        findExpiredFilesAndCloseOrWrite(true);
                        synchronized (eofLock) {
                            eofLock.notifyAll();
                        }
                    } else if (event.type == JobType.EXPIRE) {
                        findExpiredFilesAndCloseOrWrite(false);
                    }
                    return;
                }
                String datePath = dateFormatter.format(new Date(event.timestamp));
                pathKey = (event.dynamicPath == null ? datePath : (event.dynamicPath + datePath));
                writerHandler = datePathWriterMap.get(pathKey);
                if (writerHandler == null || writerHandler.closed || writerHandler.writer == null) {
                    synchronized (WriterHandler.class) {
                        if (writerHandler == null || writerHandler.closed || writerHandler.writer == null) {
                            writerHandler = new WriterHandler();
                            createFile(writerHandler, datePath, event.dynamicPath);
                            datePathWriterMap.put(pathKey, writerHandler);
                        }
                    }
                }
                writerHandler.writer.append(event.key, event.value);
                if (++flushCount >= WRITER_FLUSH_SIZE) {
                    writerHandler.lastModifyTime = System.currentTimeMillis();
                    flushCount = 0;
                    count++;
                    count = count >= Long.MAX_VALUE ? 0 : count;
                    logger.info("[Write HDFS]" + (count * WRITER_FLUSH_SIZE));
                    writerHandler.writer.sync();
                    writerHandler.writer.hflush();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                if (writerHandler != null) {
                    if (StringUtils.isNotBlank(pathKey)) {
                        datePathWriterMap.remove(pathKey);
                    }
                    close(writerHandler);
                }
            } catch (Throwable e) {
                if (event.errCallback != null) {
                    event.errCallback.run();
                }
                logger.error("[Write HDFS ERROR]", e);
                e.printStackTrace();
            }
        }

        private void createFile(final WriterHandler writerHandler, final String datePath, final String dynamicPath)
                throws Exception {
            FileSystem fs = FileSystem.get(
                    new Path((dynamicPath == null ? staticPath : dynamicPath) + fileName).toUri(), conf);
            String pathStr = staticPath + datePath + fileName + "_" + LOCAL_IP_STR + "_" + uniqId + "_";
            Path path;
            int postfix = 0;
            while (true) {
                path = new Path(pathStr + (postfix++) + EXTENTION);
                if (!fs.exists(path)) {
                    break;
                }
            }
            logger.warn("Creating File (date={}) at real path: {}", new Object[]{datePath, path});
            writerHandler.writer = SequenceFile.createWriter(conf, SequenceFile.Writer.file(path),
                    SequenceFile.Writer.keyClass(Text.class),
                    SequenceFile.Writer.valueClass(Text.class),
                    SequenceFile.Writer.compression(SequenceFile.CompressionType.BLOCK, codec));
            writerHandler.path = path;
            writerHandler.expireTime = getExpireTime();
            writerHandler.closed = false;
        }

        private void findExpiredFilesAndCloseOrWrite(final boolean force) throws IOException {
            final long now = System.currentTimeMillis();
            Iterator<Entry<String, WriterHandler>> iterator = datePathWriterMap.entrySet().iterator();
            while (iterator.hasNext()) {
                WriterHandler handler = iterator.next().getValue();
                if (force || handler.expireTime <= now) {
                    iterator.remove();
                    close(handler);
                }
            }
        }

        private long getExpireTime() {
            long now = System.currentTimeMillis();
            long currentHour = DateUtils.truncateToHour(now);
            // 后半小时创建的文件，在下个小时 10 分的时候滚动；
            // 前半小时创建的文件，在当前小时 40 分的时候滚动。
            // 这样做的原因是因为写 Seq 文件必须关闭才能让 MapReduce 看到所有数据块的内容。
            if (now - currentHour > TimeUnit.MINUTES.toMillis(30)) {
                return currentHour + TimeUnit.MINUTES.toMillis(60 + 10);
            } else {
                return currentHour + TimeUnit.MINUTES.toMillis(40);
            }
        }

        private void close(final WriterHandler handler) {
            if (!handler.closed && handler.writer != null) {
                final SequenceFile.Writer writer = handler.writer;
                final Path path = handler.path;
                handler.closed = true;
                handler.writer = null;
                handler.path = null;
                try {
                    writer.sync();
                    writer.hsync();
                    logger.warn("Closed WriterHandler successfully, path={}", path);
                } catch (Exception e) {
                    logger.warn("Fail to close WriterHandler, path={}", path, e);
                } finally {
                    IOUtils.closeStream(writer);
                }
            }
        }
    }

    private Configuration buildConfig() {
        Configuration conf = new Configuration();
        conf.addResource("hadoop-site.xml");
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, String>> iterator = conf.iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            sb.append("\n").append(entry);
        }
        logger.warn("Configuration:" + sb.toString());
        return conf;
    }
}
