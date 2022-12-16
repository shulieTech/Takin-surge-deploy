package io.shulie.takin.kafka.receiver.util;

import lombok.Getter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="tangyuhan@shulie.io">yuhan.tang</a>
 * @package: com.pamirs.takin.common.influxdb
 * @date 2020-04-20 14:25
 */
@Component
public class InfluxDatabaseWriter {
    private static final Logger logger = LoggerFactory.getLogger(InfluxDatabaseWriter.class);
    private static final ThreadLocal<InfluxDB> CACHE = new InheritableThreadLocal<>();

    /**
     * 连接地址
     */
    @Value("${spring.influxdb.url}")
    @Getter
    private String influxdbUrl;

    /**
     * 用户名
     */
    @Value("${spring.influxdb.user}")
    private String userName;

    /**
     * 密码
     */
    @Value("${spring.influxdb.password}")
    private String password;

    /**
     * 数据库库名
     */
    private final String database = "performance";

    public static BatchPoints batchPoints(String sdatabase) {
        return BatchPoints.database(sdatabase)
                .build();
    }

    private InfluxDB createInfluxDatabase() {
        try {
            return InfluxDBFactory.connect(influxdbUrl, userName, password);
        } catch (Throwable e) {
            logger.error("influxdb init fail " + ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    private InfluxDB getInfluxDatabase() {
        InfluxDB influxDatabase = CACHE.get();
        if (influxDatabase == null) {
            influxDatabase = createInfluxDatabase();
            CACHE.set(influxDatabase);
        }
        return influxDatabase;
    }

    /**
     * 插入数据
     *
     * @param measurement 表名
     * @param tags        标签
     * @param fields      字段
     * @param time        时间
     *
     * @return
     */
    public boolean insert(String measurement, Map<String, String> tags, Map<String, Object> fields, long time) {
        Point.Builder builder = Point.measurement(measurement);
        builder.tag(tags);
        builder.fields(fields);
        if (time > 0) {
            builder.time(time, TimeUnit.MILLISECONDS);
        }
        try {
            getInfluxDatabase().write(database, "", builder.build());
        } catch (Exception ex) {
            logger.error("插入数据出错[io.shulie.takin.web.data.common.InfluxDBWriter.insert].", ex);
            return false;
        }
        return true;
    }

    /**
     * 查询数据
     *
     * @return
     */
    public List<QueryResult.Result> select(String command) {
        QueryResult queryResult = getInfluxDatabase().query(new Query(command, database));
        return queryResult.getResults();
    }

    /**
     * 创建数据库
     *
     * @param dbName
     */
    public void createDatabase(String dbName) {
        getInfluxDatabase().setDatabase(dbName);
    }


    /**
     * 设置数据保存策略
     * defalut 策略名 /database 数据库名/ 30d 数据保存时限30天/ 1
     * 副本个数为1/ 结尾DEFAULT 表示 设为默认的策略
     */
    public void createRetentionPolicy() {
        String command = String.format("CREATE RETENTION POLICY \"%s\" ON \"%s\" DURATION %s REPLICATION %s DEFAULT",
                "defalut", database, "30d", 1);
        getInfluxDatabase().query(new Query(command, database));
    }

}
