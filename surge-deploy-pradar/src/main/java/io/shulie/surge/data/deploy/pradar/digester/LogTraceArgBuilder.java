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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/4/12 22:10
 */
public class LogTraceArgBuilder {

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
        args[index++] = JSON.toJSONString(rpcBased.getAttributes());
        args[index++] = JSON.toJSONString(rpcBased.getLocalAttributes());
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

        args[index++] = "";
        args[index++] = TraceFlagEnum.LOG_OK.getCode();

        //放入attachment
        AttachmentBased attachmentBased = rpcBased.getAttachmentBased();
        if (attachmentBased != null) {
            args[index++] = attachmentBased.getTemplateId() + "@##" + attachmentBased.getExt();
        } else {
            args[index++] = "";
        }

        return args;
    }
}
