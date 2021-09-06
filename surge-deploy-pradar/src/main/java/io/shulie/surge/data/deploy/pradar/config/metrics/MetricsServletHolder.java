/*
package io.shulie.surge.data.deploy.pradar.config.metrics;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.pamirs.pradar.log.parser.metrics.MetricsBased;
import io.shulie.surge.data.runtime.digest.DigestContext;
import io.shulie.surge.data.runtime.processor.DataQueue;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

*/
/**
 * @author vincent
 *//*

public class MetricsServletHolder extends HttpServlet {

    private static final long MAX_LOG_STORE_TIME_BEFORE = TimeUnit.MINUTES.toMillis(5);

    private static final Logger logger = LoggerFactory.getLogger(MetricsServletHolder.class);

    private DataQueue processor;

    public MetricsServletHolder(DataQueue processor) {
        this.processor = processor;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = "success";
        try {
            String reqData = IOUtils.toString(request.getInputStream());
            if (StringUtils.isNotBlank(reqData)) {
                List<DigestContext<MetricsBased>> publishContextList = Lists.newArrayList();
                long now = System.currentTimeMillis();
                List<FluxInfoVO> fluxInfoVOS = JSONObject.parseArray(reqData, FluxInfoVO.class);
                fluxInfoVOS.stream().filter(flux ->
                        flux.getTimestamp() <= System.currentTimeMillis() - MAX_LOG_STORE_TIME_BEFORE)
                        .forEach(flux -> {
                            MetricsBased metricsBased = FluxInfoVO.convertMetricsBased(flux);
                            DigestContext<MetricsBased> digestContext = new DigestContext();
                            digestContext.setContent(metricsBased);
                            digestContext.setProcessTime(now);
                            publishContextList.add(digestContext);
                        });
                if (CollectionUtils.isNotEmpty(publishContextList)) {
                    processor.publish(publishContextList);
                }
            }
        } catch (Throwable e) {
            logger.error("解析参数错误" + ExceptionUtils.getStackTrace(e));
            message = ExceptionUtils.getStackTrace(e);
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().println("{\"success\":\"true\",\"message\":" + message + "}");
    }
}


*/
