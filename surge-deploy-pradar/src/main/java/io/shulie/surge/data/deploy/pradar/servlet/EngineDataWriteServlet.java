package io.shulie.surge.data.deploy.pradar.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.shulie.surge.data.deploy.pradar.common.ResponseCodeEnum;
import io.shulie.surge.data.deploy.pradar.model.EngineDateModel;
import io.shulie.surge.data.deploy.pradar.model.ResponseDataModel;
import io.shulie.surge.data.sink.influxdb.InfluxDBSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Sunsy
 * @date 2022/2/24
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Singleton
public class EngineDataWriteServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(EngineDataWriteServlet.class);

    @Inject
    private InfluxDBSupport influxDbSupport;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("receive request=======================");
        long now = System.currentTimeMillis();
        ResponseDataModel responseDataModel = new ResponseDataModel(now, ResponseCodeEnum.CODE_0000.getCode(), ResponseCodeEnum.CODE_0000.getMsg());
        try {
            String reqData = IOUtils.toString(request.getInputStream());
            if (StringUtils.isNotBlank(reqData)) {
                EngineDateModel engineDateModel = JSONObject.parseObject(reqData, EngineDateModel.class);
                Long eventTime = engineDateModel.getEventTime();
                List<Map<String, String>> tags = engineDateModel.getTag();
                List<Map<String, Object>> fields = engineDateModel.getField();

                if (engineDateModel.getEventTime() != null && StringUtils.isNotBlank(engineDateModel.getMeasurement()) && CollectionUtils.isNotEmpty(tags) && CollectionUtils.isNotEmpty(fields) && tags.size() == fields.size()) {
                    //如果事件时间超过1小时,记录日志并且返回对应响应码
                    if ((now - eventTime) > 3600 * 1000) {
                        logger.error("Receive log delay over {} mils,removeDelay,eventTime:{},processTime:{}/n,log content:{}", eventTime, now, "[tags:" + JSONArray.toJSONString(tags) + "]" + "[fields:" + JSONArray.toJSONString(fields) + "]");
                        responseDataModel.setResponseCode(ResponseCodeEnum.CODE_9995.getCode());
                        responseDataModel.setResponseMsg(ResponseCodeEnum.CODE_9995.getMsg());
                    }
                    for (int i = 0; i <= tags.size() - 1; i++) {
                        influxDbSupport.write(engineDateModel.getDatabase(), engineDateModel.getMeasurement(), tags.get(i), fields.get(i), engineDateModel.getEventTime());
                    }
                } else {
                    responseDataModel.setResponseCode(ResponseCodeEnum.CODE_9996.getCode());
                    responseDataModel.setResponseMsg(ResponseCodeEnum.CODE_9996.getMsg());
                }
            } else {
                responseDataModel.setResponseCode(ResponseCodeEnum.CODE_9997.getCode());
                responseDataModel.setResponseMsg(ResponseCodeEnum.CODE_9997.getMsg());
            }
        } catch (Exception e) {
            logger.error("engine metric data write fail.catch exception {},stack is {}", e, e.getStackTrace());
            responseDataModel.setResponseCode(ResponseCodeEnum.CODE_9999.getCode());
            responseDataModel.setResponseMsg(ResponseCodeEnum.CODE_9999.getMsg());
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().println(JSONObject.toJSONString(responseDataModel));

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }
}
