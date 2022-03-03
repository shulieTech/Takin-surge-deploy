package io.shulie.surge.data;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.shulie.surge.data.common.lifecycle.Lifecycle;
import io.shulie.surge.data.common.lifecycle.LifecycleObserver;
import io.shulie.surge.data.common.utils.IpAddressUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sunsy
 * @date 2022/2/25
 * @apiNode
 * @email sunshiyu@shulie.io
 */
public class JettySupplierObserver implements LifecycleObserver {
    private static final Logger logger = LoggerFactory.getLogger(JettySupplierObserver.class);

    @Inject
    @Named("config.gateway.apisix.enable")
    protected transient Boolean registerToGateWay;
    @Inject
    @Named("config.gateway.apisix.host")
    protected transient String host;
    @Inject
    @Named("config.gateway.apisix.port")
    protected transient int port;
    @Inject
    @Named("config.gateway.apisix.url")
    protected transient String url;
    @Inject
    @Named("config.gateway.apisix.apikey")
    protected transient String apikey;


    @Override
    public void beforeStart(Lifecycle target) {

    }

    @Override
    public void afterStart(Lifecycle target) {
        JettySupplier jettySupplier = (JettySupplier) target;
        if (registerToGateWay) {
            //调用网关服务注册
            registerToGateWay(IpAddressUtils.getLocalAddress(), jettySupplier.getPort());
        }
    }

    private Boolean registerToGateWay(String ip, int publishPort) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String uri = "http://" + host + ":" + port + url;
        HttpPatch httppatch = new HttpPatch(uri);
        httppatch.addHeader("Content-type", "application/json");
        httppatch.addHeader("X-API-KEY", apikey);

        Map<String, Object> nodes = new HashMap<>();
        Map<String, Object> weight = new HashMap<>();
        StringBuilder key = new StringBuilder(ip + ":" + publishPort);
        weight.put(key.toString(), 1);
        nodes.put("nodes", weight);

        String serviceNode = JSONObject.toJSONString(nodes);
        StringEntity stringEntity = new StringEntity(serviceNode, "utf-8");
        httppatch.setEntity(stringEntity);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httppatch);
            HttpEntity entity = response.getEntity();
            String responseMsg = EntityUtils.toString(entity, "utf-8");
            //如果返回的已注册节点不包含当前注册节点,抛出异常
            if (!((JSONObject) JSONPath.read(responseMsg, "$.node.value.nodes")).containsKey(key.toString())) {
                throw new RuntimeException("fail to register service to gateway,gateway cannot response correctly.");
            }
        } catch (Exception e) {
            throw new RuntimeException("fail to register service to gateway,service is " + serviceNode, e);
        }
        try {
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void beforeStop(Lifecycle target) {

    }

    @Override
    public void afterStop(Lifecycle target) {

    }

}
