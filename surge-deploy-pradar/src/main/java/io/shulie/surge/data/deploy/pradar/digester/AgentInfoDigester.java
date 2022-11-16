package io.shulie.surge.data.deploy.pradar.digester;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pamirs.pradar.log.parser.agent.AgentBased;
import com.pamirs.pradar.log.parser.constant.TenantConstants;
import io.shulie.surge.data.deploy.pradar.common.PradarUtils;
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

    @Inject
    @DefaultValue("false")
    @Named("/pradar/config/rt/agentInfoDisable")
    private Remote<Boolean> agentInfoDisable;

    @Inject
    @DefaultValue("4000")
    @Named("/pradar/config/rt/agentInfoLength")
    private Remote<Integer> agentInfoLength;

    @Inject
    @DefaultValue("1")
    @Named("/pradar/config/rt/agentInfoSampling")
    private Remote<Integer> agentInfoSampling;

    /**
     * 初始化任务只会执行一次
     */
    private void init() {
        //启动一个定时任务,每隔5分钟运行一次
        executor.scheduleAtFixedRate((Runnable) () -> {
            Map<String, Object> countMap = mysqlSupport.queryForMap("select count(1) as count from t_amdb_agent_info;");
            if (MapUtils.isNotEmpty(countMap) && ((long) countMap.get("count") > maxRowSize.get())) {
                logger.info("current agent log row length reach {},stop write into database.", countMap.get("count"));
                isWriteFlag = false;
            } else {
                isWriteFlag = true;
            }

            //如果当前写入标志为false
            if (!isWriteFlag) {
                //开始强制执行清理
                try {
                    //删除当前时间往前1小时的数据
                    long cleanTime = System.currentTimeMillis() - reserveHours.get() * 60 * 60 * 1000;
                    String sql = "delete from t_amdb_agent_info where agent_timestamp < " + cleanTime;
                    mysqlSupport.execute(sql);
                    logger.info("cleared {} hour's agentLog,cleared sql:{}", reserveHours.get(), sql);
                } catch (Exception e) {
                    logger.error("cleared {} hour's agentLog,failed{},exception stack:{}", reserveHours.get(), e, e.getStackTrace());
                }
            }

        }, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public void digest(DigestContext<AgentBased> context) {
        if (agentInfoDisable.get()) {
            return;
        }
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
            // 采样处理
            if (!PradarUtils.isAgentInfoSampleAccepted(agentBased, agentInfoSampling.get())) {
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

            if (agentBased.getAgentInfo().length() > agentInfoLength.get()) {
                String agentInfo = agentBased.getAgentInfo();
                logger.warn("agent log is too long:{},cut it: {}.", agentInfo.length(), agentBased);
                agentBased.setAgentInfo(agentInfo.substring(0, agentInfoLength.get()));
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
