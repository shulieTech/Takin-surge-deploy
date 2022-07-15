package io.shulie.surge.data.deploy.pradar.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
    /**
     * 边缓存
     */
    private Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(30000).expireAfterWrite(1, TimeUnit.HOURS).build();

    private static int pageSize = 500;

    @Inject
    private MysqlSupport mysqlSupport;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String QUERY_LINK_CONFIG = "select link_id from t_amdb_pradar_link_config";

    private static final String QUERY_LINK_EDGE = "select edge_id from t_amdb_pradar_link_edge where link_id='%s' and gmt_modify > '%s' limit %d,%d";


    public void init() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> initConfig(), 0, 5, TimeUnit.MINUTES);
    }

    private void initConfig() {
        try {
            // 查询前一天
            LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(1440);
            String startDate = dateTimeFormatter.format(localDateTime);
            // 获取所有链路配置
            List<Map<String, Object>> linkConfigList = mysqlSupport.queryForList(QUERY_LINK_CONFIG);
            if (CollectionUtils.isNotEmpty(linkConfigList)) {
                linkConfigList.stream().forEach(linkConfig -> {
                    Object linkId = linkConfig.get("link_id");
                    // 分页去查询,每次查询1000条数据
                    int startIndex = 1;
                    while (true) {
                        // 计算偏移量
                        int offset = (startIndex - 1) * pageSize;
                        String querySql = String.format(QUERY_LINK_EDGE, linkId, startDate, offset, pageSize);
                        List<Map<String, Object>> edgeList = mysqlSupport.queryForList(querySql);
                        if (CollectionUtils.isNotEmpty(edgeList)) {
                            edgeList.stream().forEach(edge -> {
                                String edgeId = String.valueOf(edge.get("edge_id"));
                                this.cache.put(edgeId, edgeId);
                            });
                            startIndex++;
                        } else {
                            // 遍历完成
                            break;
                        }
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
