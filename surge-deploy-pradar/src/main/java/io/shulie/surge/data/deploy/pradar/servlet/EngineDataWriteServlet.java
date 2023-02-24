package io.shulie.surge.data.deploy.pradar.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.shulie.surge.data.common.utils.TimeUtils;
import io.shulie.surge.data.deploy.pradar.common.ResponseCodeEnum;
import io.shulie.surge.data.deploy.pradar.model.EngineDateModel;
import io.shulie.surge.data.deploy.pradar.model.ResponseDataModel;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
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
import java.util.UUID;

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
    private ClickHouseShardSupport clickHouseShardSupport;

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
                //Long eventTime = engineDateModel.getEventTime();
                List<Map<String, String>> tags = engineDateModel.getTag();
                List<Map<String, Object>> fields = engineDateModel.getField();

                if (StringUtils.isNotBlank(engineDateModel.getMeasurement()) && CollectionUtils.isNotEmpty(tags) && CollectionUtils.isNotEmpty(fields) && tags.size() == fields.size()) {
                    //遍历写入每一条数据
                    for (int i = 0; i <= tags.size() - 1; i++) {
                        Map<String, Object> objectMap = fields.get(i);
                        Map<String, String> tagMap = tags.get(i);

                        if (!tagMap.containsKey("eventTime")) {
                            logger.error("req data lack eventTime,skip it.content is {}", "[tags:" + JSONArray.toJSONString(tags) + "]" + "[fields:" + JSONArray.toJSONString(fields) + "]");
                            responseDataModel.setResponseCode(ResponseCodeEnum.CODE_9994.getCode());
                            responseDataModel.setResponseMsg(ResponseCodeEnum.CODE_9994.getMsg());
                            continue;
                        }
                        Long eventTime = Long.parseLong(tagMap.get("eventTime"));
                        //如果eventTime时间格式非法
                        if (eventTime < 0) {
                            logger.error("eventTime is illegal,skip it.content is {}", "[tags:" + JSONArray.toJSONString(tags) + "]" + "[fields:" + JSONArray.toJSONString(fields) + "]");
                            responseDataModel.setResponseCode(ResponseCodeEnum.CODE_9993.getCode());
                            responseDataModel.setResponseMsg(ResponseCodeEnum.CODE_9993.getMsg());
                            continue;
                        }

                        //如果事件时间超过1小时,记录日志并且返回对应响应码
                        if ((now - eventTime) > 3600 * 1000) {
                            logger.error("Receive log delay over {} mils,removeDelay,eventTime:{},processTime:{}/n,log content:{}", eventTime, now, "[tags:" + JSONArray.toJSONString(tags) + "]" + "[fields:" + JSONArray.toJSONString(fields) + "]");
                            responseDataModel.setResponseCode(ResponseCodeEnum.CODE_9995.getCode());
                            responseDataModel.setResponseMsg(ResponseCodeEnum.CODE_9995.getMsg());
                        }
                        //打标签
                        tagMap.put("timeWindow5", String.valueOf(TimeUtils.getTimeWindow(eventTime, 1).getTime().getTime()));
                        tagMap.put("timeWindow10", String.valueOf(TimeUtils.getTimeWindow(eventTime, 2).getTime().getTime()));
                        tagMap.put("timeWindow30", String.valueOf(TimeUtils.getTimeWindow(eventTime, 3).getTime().getTime()));
                        objectMap.putAll(tagMap);
                        String tableName = clickHouseShardSupport.isCluster() ? engineDateModel.getMeasurement() : engineDateModel.getMeasurement() + "_all";
                        clickHouseShardSupport.insert(objectMap, UUID.randomUUID().toString(), tableName);
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
