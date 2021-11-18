package io.shulie.surge.data.deploy.pradar.digester;

import com.google.inject.Inject;
import com.pamirs.pradar.log.parser.agent.AgentBased;
import com.pamirs.pradar.log.parser.constant.TenantConstants;
import io.shulie.surge.data.deploy.pradar.model.AgentInfoModel;
import io.shulie.surge.data.runtime.common.utils.ApiProcessor;
import io.shulie.surge.data.runtime.digest.DataDigester;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.sink.mysql.MysqlSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
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

    @Override
    public void digest(DigestContext<AgentBased> context) {
        AgentBased agentBased = context.getContent();
        try {
            if (agentBased == null) {
                return;
            }

            //校验ip是否合法,如果不合法,需要手动构造一条数据写入mysql
            if (!Pattern.matches(pattern, agentBased.getIp())) {
                logger.warn("detect illegal agent log:{},skip it.", agentBased);
                return;
            }

            //对于1.1以及之前的老版本探针,没有租户相关字段,根据应用名称获取租户配置,没有设默认值
            if (StringUtils.isBlank(agentBased.getUserAppKey())) {
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
