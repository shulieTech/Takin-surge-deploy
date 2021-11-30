package io.shulie.surge.data.deploy.pradar.model;

import com.pamirs.pradar.log.parser.agent.AgentBased;

/**
 * @author anjone
 * @date 2021/8/17
 */
public class AgentInfoModel {

    public static String insertSql = "insert into t_amdb_agent_info(agent_id,app_name,ip,port,user_app_key,env_code,user_id,agent_info,agent_timestamp)" +
            " values (?,?,?,?,?,?,?,?,?)";

    public static Object[] values(AgentBased agentBased) {
        return new Object[]{agentBased.getAgentId(), agentBased.getAppName(), agentBased.getIp(), agentBased.getPort(), agentBased.getUserAppKey(), agentBased.getEnvCode(), agentBased.getUserId(), agentBased.getAgentInfo(), agentBased.getTimestamp()};
    }

}
