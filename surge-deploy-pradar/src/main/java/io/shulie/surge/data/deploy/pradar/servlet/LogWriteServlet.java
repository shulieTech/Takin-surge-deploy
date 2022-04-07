package io.shulie.surge.data.deploy.pradar.servlet;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.pamirs.pradar.remoting.protocol.CommandCode;
import io.shulie.surge.data.deploy.pradar.common.ResponseCodeEnum;
import io.shulie.surge.data.deploy.pradar.model.ResponseDataModel;
import io.shulie.surge.data.runtime.disruptor.RingBufferIllegalStateException;
import io.shulie.surge.data.runtime.processor.DataQueue;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Sunsy
 * @date 2022/2/24
 * @apiNode
 * @email sunshiyu@shulie.io
 */
@Singleton
public class LogWriteServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(LogWriteServlet.class);

    protected Map<String, DataQueue> queueMap;

    public Map<String, DataQueue> getQueueMap() {
        return queueMap;
    }

    public void setQueueMap(Map<String, DataQueue> queueMap) {
        this.queueMap = queueMap;
    }

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
        long start = System.currentTimeMillis();
        //logger.info("receive log =======================");
        String content = null;
        long uploadTime = Long.parseLong(request.getHeader("time"));
        ResponseDataModel responseDataModel = new ResponseDataModel(String.valueOf(CommandCode.SUCCESS), ResponseCodeEnum.CODE_0000.getMsg());
        try {
            String encode = request.getHeader("Accept-Encoding");
            //如果含有gzip压缩头,使用gzip解压
            if (StringUtils.isNotBlank(encode) && encode.contains("gzip")) {
                content = decompress(request.getInputStream());
            } else {
                content = IOUtils.toString(request.getInputStream(), "utf-8");
            }
            if (StringUtils.isNotBlank(content)) {
                String hostIp = request.getHeader("hostIp");
                String dataVersion = request.getHeader("version");
                String dataType = request.getHeader("dataType");
                if (StringUtils.isBlank(hostIp) || StringUtils.isBlank(dataVersion) || StringUtils.isBlank(dataType)) {
                    responseDataModel.setResponseMsg("缺少header:hostIp or version or dataType");
                    responseDataModel.setResponseCode(String.valueOf(CommandCode.COMMAND_CODE_NOT_SUPPORTED));
                } else {
                    byte dateTypeByte = 0;
                    switch (dataType) {
                        case "1":
                            dateTypeByte = 1;
                            //logger.info("current log type is trace log.");
                            break;
                        case "2":
                            dateTypeByte = 2;
                            break;
                        case "3":
                            dateTypeByte = 3;
                            break;
                        case "4":
                            dateTypeByte = 4;
                            break;
                        default:
                            break;
                    }
                    DataQueue queue = queueMap.get(dataType);
                    Map<String, Object> header = Maps.newHashMap();
                    header.put("hostIp", hostIp);
                    header.put("dataVersion", dataVersion);
                    header.put("dataType", dateTypeByte);
                    header.put("uploadTime", uploadTime);
                    queue.publish(header, queue.splitLog(content, dateTypeByte));
                }
            } else {
                responseDataModel.setResponseMsg("日志内容为空");
                responseDataModel.setResponseCode(String.valueOf(CommandCode.COMMAND_CODE_NOT_SUPPORTED));
            }

        } catch (RingBufferIllegalStateException e) {
            logger.error(e.getMessage());
            responseDataModel.setResponseMsg("系统繁忙");
            responseDataModel.setResponseCode(String.valueOf(CommandCode.SYSTEM_BUSY));
        } catch (Throwable e) {
            logger.error("logProcessor fail " + ExceptionUtils.getStackTrace(e));
            responseDataModel.setResponseMsg("处理异常");
            responseDataModel.setResponseCode(String.valueOf(CommandCode.SYSTEM_ERROR));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=utf-8");
        responseDataModel.setTime(System.currentTimeMillis());
        response.getWriter().println(JSONObject.toJSONString(responseDataModel));
        logger.info("processed log,cost is {},event delay is {}", System.currentTimeMillis() - start, start - uploadTime);
    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }


    // 解压缩
    private String decompress(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPInputStream gunzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gunzip.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        // toString()使用平台默认编码，也可以显式的指定如toString("GBK")
        return out.toString("utf-8");
    }
}
