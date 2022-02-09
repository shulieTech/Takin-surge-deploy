package io.shulie.surge.data.deploy.pradar.digester;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.agent.AgentBased;
import com.pamirs.pradar.log.parser.constant.TenantConstants;
import io.shulie.surge.data.deploy.pradar.model.AgentInfoModel;
import io.shulie.surge.data.runtime.common.remote.DefaultValue;
import io.shulie.surge.data.runtime.common.remote.Remote;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * agent上报日志
 *
 * @author anjone
 * @date 2021/8/17
 */
public class AgentInfoDigester implements DataDigester<AgentBased> {
    private static final Logger logger = LoggerFactory.getLogger(AgentInfoDigester.class);

    @Inject
    private MysqlSupport mysqlSupport;

    private String pattern = "^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$";

    private transient AtomicBoolean isRunning = new AtomicBoolean(false);

    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private volatile boolean isWriteFlag = true;

    @Inject
    @DefaultValue("1000000")
    @Named("/pradar/config/rt/maxRowSize")
    private Remote<Long> maxRowSize;

    @Inject
    @DefaultValue("24")
    @Named("/pradar/config/rt/reserveHours")
    private Remote<Integer> reserveHours;

    /**
     * 初始化任务只会执行一次
     */
    private void init() {
        //启动一个定时任务,每隔5分钟运行一次
        executor.scheduleAtFixedRate((Runnable) () -> {
            //如果当前写入标志为false
            if (!isWriteFlag) {
                //开始强制执行清理
                try {
                    //获取数据库时间,防止和现实时间不一致
                    Long time = mysqlSupport.queryForObject("select max(agent_timestamp) as time from t_amdb_agent_info", Long.class);
                    //如果表里没数据,不执行删除
                    if (time == null) {
                        return;
                    }
                    long cleanTime = time - reserveHours.get() * 60 * 60 * 1000;
                    String sql = "delete from t_amdb_agent_info where agent_timestamp < " + cleanTime;
                    mysqlSupport.execute(sql);
                    logger.info("已清理{}小时前agentlog,清理sql:{}", reserveHours.get(), sql);
                } catch (Exception e) {
                    logger.error("清理{}天前agentlog数据失败{},异常堆栈:{}", reserveHours.get(), e, e.getStackTrace());
                }
            }

            Map<String, Object> countMap = mysqlSupport.queryForMap("select count(1) as count from t_amdb_agent_info;");
            if (MapUtils.isNotEmpty(countMap) && ((long) countMap.get("count") > maxRowSize.get())) {
                logger.info("current agent log row length reach {},stop write into database.", countMap.get("count"));
                isWriteFlag = false;
            } else {
                isWriteFlag = true;
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public void digest(DigestContext<AgentBased> context) {
        if (!isWriteFlag) {
            return;
        }
        if (isRunning.compareAndSet(false, true)) {
            init();
        }

        AgentBased agentBased = context.getContent();
        try {
            if (agentBased == null) {
                return;
            }

            if (!Pattern.matches(pattern, agentBased.getIp())) {
                logger.warn("detect illegal agent log:{},skip it.", agentBased);
                return;
            }

            //对于1.1以及之前的老版本探针,没有租户相关字段,根据应用名称获取租户配置,没有设默认值
            if (StringUtils.isBlank(agentBased.getUserAppKey()) || TenantConstants.DEFAULT_USER_APP_KEY.equals(agentBased.getUserAppKey())) {
                agentBased.setUserAppKey(ApiProcessor.getTenantConfigByAppName(agentBased.getAppName()).get("tenantAppKey"));
            }
            if (StringUtils.isBlank(agentBased.getEnvCode())) {
                agentBased.setEnvCode(ApiProcessor.getTenantConfigByAppName(agentBased.getAppName()).get("envCode"));
            }
            if (StringUtils.isBlank(agentBased.getUserId())) {
                agentBased.setUserId(TenantConstants.DEFAULT_USERID);
            }

            if (agentBased.getAgentInfo().length() > 40000) {
                String agentInfo = agentBased.getAgentInfo();
                logger.warn("agent log is too long:{},cut it: {}.", agentInfo.length(), agentBased);
                agentBased.setAgentInfo(agentInfo.substring(0, 40000));
                //help gc
                agentInfo = null;
            }
            mysqlSupport.batchUpdate(AgentInfoModel.insertSql, Collections.singletonList(AgentInfoModel.values(agentBased)));
        } catch (Throwable e) {
            logger.warn("fail to write mysql, log: " + agentBased.getLog() + ", error:" + ExceptionUtils.getStackTrace(e));
        }

    }

    @Override
    public int threadCount() {
        return 1;
    }

    @Override
    public void stop() throws Exception {
        try {
            this.mysqlSupport.stop();
        } catch (Throwable e) {
            logger.error("mysql stop fail");
        }
    }
}
