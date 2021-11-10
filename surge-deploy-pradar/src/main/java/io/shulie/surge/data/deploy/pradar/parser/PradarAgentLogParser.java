package io.shulie.surge.data.deploy.pradar.parser;

import com.pamirs.pradar.log.parser.ProtocolParserFactory;
import com.pamirs.pradar.log.parser.agent.AgentBased;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.runtime.parser.DataParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PradarAgentLogParser implements DataParser<String, AgentBased> {

    private static final Logger logger = LoggerFactory.getLogger(PradarAgentLogParser.class);

    private static final String HEADER_DATA_VERSION = "dataVersion";
    private static final String HEADER_HOST_IP = "hostIp";

    /**
     * 创建数据处理上下文
     *
     * @param header
     * @param content
     * @return
     */
    @Override
    public DigestContext<AgentBased> createContext(Map<String, Object> header, String content) {
        String dataVersion = String.valueOf(header.getOrDefault(HEADER_DATA_VERSION, "1.0"));
        String hostIp = String.valueOf(header.getOrDefault(HEADER_HOST_IP, ""));
        content += '|' + hostIp + '|' + dataVersion;
        long now = System.currentTimeMillis();
        AgentBased agentBased = ProtocolParserFactory.getFactory().getAgentProtocolParser(dataVersion).parse(hostIp, dataVersion, content);
        if (agentBased == null) {
            logger.warn("cannot parse log:" + content);
            return null;
        }

        agentBased.setLog(content);

        DigestContext<AgentBased> context = new DigestContext<>();
        context.setContent(agentBased);
        context.setHeader(header);
        context.setProcessTime(now);
        context.setEventTime(agentBased.getLogTime());
        return context;
    }


}
