//package io.shulie.surge.data.deploy.pradar.digester;
//
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//import com.google.inject.name.Named;
//import com.pamirs.pradar.log.parser.DataType;
//import com.pamirs.pradar.log.parser.trace.RpcBased;
//import io.shulie.surge.data.HDFSSupport;
//import io.shulie.surge.data.deploy.pradar.common.PradarUtils;
//import io.shulie.surge.data.runtime.common.remote.DefaultValue;
//import io.shulie.surge.data.runtime.common.remote.Remote;
//import io.shulie.surge.data.runtime.digest.DataDigester;
//import io.shulie.surge.data.runtime.digest.DigestContext;
//import org.apache.log4j.Logger;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * 日志原文按插入顺序直接存储-写入HDFS
// *
// * @author xingchen
// */
////@Singleton
//public class SequenceLogDigester implements DataDigester<RpcBased> {
//    private static final Logger logger = Logger.getLogger(SequenceLogDigester.class);
//    @Inject
//    private HDFSSupport hdfsSequenceStorer;
//
//    @Inject
//    @DefaultValue("false")
//    @Named("/pradar/config/rt/hdfsDisable")
//    private Remote<Boolean> hdfsDisable;
//
//    @Inject
//    @DefaultValue("1")
//    @Named("/pradar/config/rt/hdfsSampling")
//    private Remote<Integer> hdfsSampling;
//
//    /*
//     * 处理日志的时间范围
//     */
//    private static final long MAX_LOG_STORE_TIME_BEFORE = TimeUnit.HOURS.toMillis(1);
//    private static final long MAX_LOG_STORE_TIME_AFTER = TimeUnit.HOURS.toMillis(1);
//
//    @Override
//    public void digest(DigestContext<RpcBased> context) {
//        if (hdfsDisable.get()) {
//            return;
//        }
//        try {
//            RpcBased rpcBased = context.getContent();
//            if (!PradarUtils.isTraceSampleAccepted(rpcBased.getTraceId(), hdfsSampling.get())) {
//                return;
//            }
//            // 按 traceId 里面的时间对齐，如果太晚了，直接丢弃
//            final long processTime = context.getProcessTime();
//            final long traceTime = context.getEventTime();
//            if (traceTime <= processTime - MAX_LOG_STORE_TIME_BEFORE || traceTime >= processTime + MAX_LOG_STORE_TIME_AFTER) {
//                return;
//            }
//            hdfsSequenceStorer.write(rpcBased.getHostIp(), context.getContent().getLog(), rpcBased.getStartTime(), null, null);
//        } catch (Exception e) {
//            logger.warn("fail to write HDFS, log: " + context.getContent() + ", error:" + e.getMessage());
//        }
//    }
//
//
//    @Override
//    public void stop() throws Exception {
//        hdfsSequenceStorer.stop();
//    }
//
//    @Override
//    public int threadCount() {
//        return 1;
//    }
//}
