package io.shulie.surge.data.deploy.pradar.model;

import com.pamirs.pradar.log.parser.agent.AgentBased;
import io.shulie.surge.data.deploy.pradar.parser.utils.Md5Utils;

/**
 * @author anjone
 * @date 2021/8/17
 */
public class AgentInfoModel {

    public static String insertSql = "insert into t_amdb_agent_info(agent_id,app_name,ip,port,user_app_key,env_code,user_id,agent_info,agent_timestamp,md5)" +
            " values (?,?,?,?,?,?,?,?,?,?) ";

    public static Object[] values(AgentBased agentBased) {
        return new Object[]{agentBased.getAgentId(), agentBased.getAppName(), agentBased.getIp(), agentBased.getPort(), agentBased.getUserAppKey(), agentBased.getEnvCode(), agentBased.getUserId(), agentBased.getAgentInfo(), agentBased.getTimestamp(), Md5Utils.md5(new StringBuilder().append(agentBased.getUserAppKey()).append(agentBased.getEnvCode()).append(agentBased.getAppName()).append(agentBased.getAgentId()).append(agentBased.getAgentInfo()).toString())};
    }

}
