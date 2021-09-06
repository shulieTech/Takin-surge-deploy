package io.shulie.surge.data.deploy.pradar.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author pamirs
 */
@Singleton
public class EagleLoader {
    /**
     * 边缓存
     */
    private Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(1, TimeUnit.HOURS).build();

    @Inject
    private MysqlSupport mysqlSupport;

    private static final String QUERY_LINK_CONFIG = "select link_id from t_amdb_pradar_link_config";

    private static final String QUERY_LINK_EDGE = "select edge_id from t_amdb_pradar_link_edge where link_id='%s' " +
            " and gmt_modify >DATE_ADD(NOW(), INTERVAL -120 MINUTE) " +
            " order by gmt_modify desc limit 101";
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
                    List<Map<String, Object>> edgeList = mysqlSupport.queryForList(String.format(QUERY_LINK_EDGE, linkId));
                    if (CollectionUtils.isNotEmpty(edgeList)&&edgeList.size()<100) {
                        edgeList.stream().forEach(edge->{
                            String edgeId = String.valueOf(edge.get("edge_id"));
                            this.cache.put(edgeId,edgeId);
                        });
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
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
