package io.shulie.surge.data.deploy.pradar.digester;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.pamirs.pradar.log.parser.constant.TenantConstants;
import com.pamirs.pradar.log.parser.trace.AttachmentBased;
import com.pamirs.pradar.log.parser.trace.RpcBased;
import io.shulie.surge.data.deploy.pradar.common.MiddlewareTypeEnum;
import io.shulie.surge.data.deploy.pradar.common.TraceFlagEnum;
import io.shulie.surge.data.deploy.pradar.parser.PradarLogType;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParser;
import io.shulie.surge.data.deploy.pradar.parser.RpcBasedParserFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/4/12 22:10
 */
public class LogTraceArgBuilder {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<String> metas() {
        List<String> meta = new ArrayList<>();
        meta.add("appName");
        meta.add("entranceId");
        meta.add("entranceNodeId");
        meta.add("traceId");
        meta.add("level");
        meta.add("parentIndex");
        meta.add("`index`");
        meta.add("rpcId");
        meta.add("rpcType");
        meta.add("logType");
        meta.add("traceAppName");
        meta.add("upAppName");
        meta.add("startTime");
        meta.add("cost");
        meta.add("middlewareName");
        meta.add("serviceName");
        meta.add("methodName");
        meta.add("remoteIp");
        meta.add("port");
        meta.add("resultCode");
        meta.add("requestSize");
        meta.add("responseSize");
        meta.add("request");
        meta.add("response");
        meta.add("clusterTest");
        meta.add("callbackMsg");
        meta.add("samplingInterval");
        meta.add("localId");
        meta.add("attributes");
        meta.add("localAttributes");
        meta.add("async");
        meta.add("version");
        meta.add("hostIp");
        meta.add("agentId");
        meta.add("startDate");
        meta.add("receiveTime");
        meta.add("processTime");
        meta.add("uploadTime");
        meta.add("receiveHttpTime");
        meta.add("taskId");
        meta.add("userAppKey");
        meta.add("envCode");
        meta.add("userId");

        meta.add("timeMin");
        meta.add("dateToMin");
        meta.add("parsedServiceName");
        meta.add("parsedMethod");
        meta.add("parsedAppName");
        meta.add("parsedExtend");
        meta.add("parsedMiddlewareName");
        meta.add("entranceServiceType");

        meta.add("flag");
        meta.add("flagMessage");
        return meta;
    }

    public String getParam() {
        List<String> meta = metas();
        List<String> list = Lists.newArrayList();
        for (String key : meta) {
            list.add("?");
        }
        return Joiner.on(',').join(list).toString();
    }

    public String getCols() {
        return Joiner.on(',').join(metas()).toString();
    }

    public Object[] buildArg(RpcBased rpcBased) {
        Object[] args = new Object[53];
        int index = 0;
        long time1 = System.currentTimeMillis();
        args[index++] = rpcBased.getAppName();
        args[index++] = rpcBased.getEntranceId();
        args[index++] = rpcBased.getEntranceNodeId();
        args[index++] = rpcBased.getTraceId();
        args[index++] = rpcBased.getLevel();
        args[index++] = rpcBased.getParentIndex();
        args[index++] = rpcBased.getIndex();
        args[index++] = rpcBased.getRpcId();
        args[index++] = rpcBased.getRpcType();
        args[index++] = rpcBased.getLogType();
        args[index++] = rpcBased.getTraceAppName();
        args[index++] = rpcBased.getUpAppName();
        args[index++] = rpcBased.getStartTime();
        args[index++] = rpcBased.getCost();
        args[index++] = rpcBased.getMiddlewareName();
        args[index++] = rpcBased.getServiceName();
        args[index++] = rpcBased.getMethodName();
        args[index++] = rpcBased.getRemoteIp();
        long time2 = System.currentTimeMillis();
        args[index++] = NumberUtils.toInt(rpcBased.getPort(), 0);
        args[index++] = rpcBased.getResultCode();
        args[index++] = rpcBased.getRequestSize();
        args[index++] = rpcBased.getResponseSize();
        args[index++] = rpcBased.getRequest();
        args[index++] = rpcBased.getResponse();
        args[index++] = rpcBased.isClusterTest();
        args[index++] = rpcBased.getCallbackMsg();
        args[index++] = rpcBased.getSamplingInterval();
        args[index++] = rpcBased.getLocalId();
        long time3 = System.currentTimeMillis();
        args[index++] = JSON.toJSONString(rpcBased.getAttributes());
        args[index++] = JSON.toJSONString(rpcBased.getLocalAttributes());
        long time4 = System.currentTimeMillis();
        args[index++] = rpcBased.isAsync();
        args[index++] = rpcBased.getVersion();
        args[index++] = rpcBased.getHostIp();
        args[index++] = rpcBased.getAgentId();
        args[index++] = new Date(rpcBased.getStartTime());
        args[index++] = rpcBased.getDataLogTime();
        args[index++] = System.currentTimeMillis();
        args[index++] = rpcBased.getUploadTime();
        args[index++] = rpcBased.getReceiveHttpTime();
        args[index++] = rpcBased.getTaskId();

        args[index++] = rpcBased.getUserAppKey();
        args[index++] = rpcBased.getEnvCode();
        args[index++] = StringUtils.isBlank(rpcBased.getUserId()) ? TenantConstants.DEFAULT_USERID : rpcBased.getUserId();
        args[index++] = rpcBased.getStartTime() / 1000 / 60;
        args[index++] = rpcBased.getStartTime() / 1000 / 60 / 60 / 24;

        long time5 = System.currentTimeMillis();

        RpcBasedParser rpcBasedParser = RpcBasedParserFactory.getInstance(rpcBased.getLogType(), rpcBased.getRpcType());
        if (rpcBasedParser != null) {
            args[index++] = StringUtils.defaultString(rpcBasedParser.serviceParse(rpcBased), "");
            args[index++] = StringUtils.defaultString(rpcBasedParser.methodParse(rpcBased), "");
            args[index++] = StringUtils.defaultString(rpcBasedParser.appNameParse(rpcBased), "");
            args[index++] = StringUtils.defaultString(rpcBasedParser.extendParse(rpcBased), "");
            args[index++] = MiddlewareTypeEnum.getNodeType(rpcBased.getMiddlewareName()).getType();
        } else {
            if (rpcBased.getLogType() == PradarLogType.LOG_TYPE_FLOW_ENGINE) {
                args[index++] = rpcBased.getServiceName();
                args[index++] = rpcBased.getMethodName();
                args[index++] = rpcBased.getAppName();
            } else {
                args[index++] = "";
                args[index++] = "";
                args[index++] = "";
            }
            args[index++] = "";
            args[index++] = "";
        }
        long time6 = System.currentTimeMillis();
        args[index++] = "";
        args[index++] = TraceFlagEnum.LOG_OK.getCode();

        //放入attachment
        AttachmentBased attachmentBased = rpcBased.getAttachmentBased();
        if (attachmentBased != null) {
            args[index++] = attachmentBased.getTemplateId() + "@##" + attachmentBased.getExt();
        } else {
            args[index++] = "";
        }
        long time7 = System.currentTimeMillis();
        if (time7 - time1 > 10) {
            logger.info("LogTraceArgBuilder cost={}. sc1={}, sc2={},sc3={},sc4={},sc5={},sc5={}", time7 - time1, time2 - time1, time3 - time2, time4 - time3, time5 - time4, time6 - time5, time7 - time6);
        }

        return args;
    }
}
