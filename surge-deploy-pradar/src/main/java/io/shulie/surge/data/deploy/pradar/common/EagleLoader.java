package io.shulie.surge.data.deploy.pradar.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author pamirs
 */
@Singleton
public class EagleLoader {
    private static Logger logger = LoggerFactory.getLogger(EagleLoader.class);

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 边缓存
     */
    private Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(60000).expireAfterWrite(1, TimeUnit.HOURS).build();

    @Inject
    private MysqlSupport mysqlSupport;

    private static final String QUERY_LINK_CONFIG = "select link_id from t_amdb_pradar_link_config";

    private static final String QUERY_LINK_EDGE = "select edge_id from t_amdb_pradar_link_edge where link_id='%s' order by gmt_modify desc limit %d";

    private static final String QUERY_LINK_EDGE_COUNT = "select count(*) from t_amdb_pradar_link_edge where link_id='%s'";

    private static int pageSize = 1000;

    public void init() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> initConfig(), 0, 5, TimeUnit.MINUTES);
    }

    private void initConfig() {
        try {
            // 获取所有链路配置
            List<Map<String, Object>> linkConfigList = mysqlSupport.queryForList(QUERY_LINK_CONFIG);
            if (CollectionUtils.isNotEmpty(linkConfigList)) {
                linkConfigList.stream().forEach(linkConfig -> {
                    Object linkId = linkConfig.get("link_id");
                    String countSql = String.format(QUERY_LINK_EDGE_COUNT, linkId);
                    Integer count = mysqlSupport.queryForObject(countSql, Integer.class);
                    if (count > pageSize) {
                        logger.warn("当前{}对应的边已超1000,请检查！！！", linkId);
                    }
                    String querySql = String.format(QUERY_LINK_EDGE, linkId, pageSize);
                    List<Map<String, Object>> edgeList = mysqlSupport.queryForList(querySql);
                    if (CollectionUtils.isNotEmpty(edgeList)) {
                        edgeList.stream().forEach(edge -> {
                            String edgeId = String.valueOf(edge.get("edge_id"));
                            // value设置为空
                            this.cache.put(edgeId, "1");
                        });
                    }
                });
            }
        } catch (Throwable e) {
            logger.warn("链路边缓存异常," + ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 此边是否需要统计流量
     */
    public boolean contains(String key) {
        try {
            String value = this.cache.getIfPresent(key);
            if (StringUtils.isNotBlank(value)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
