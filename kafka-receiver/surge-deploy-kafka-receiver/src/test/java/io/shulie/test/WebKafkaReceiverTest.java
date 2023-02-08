package io.shulie.test;
import java.util.Date;

import io.shulie.takin.kafka.receiver.Application;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.shulie.takin.kafka.receiver.constant.web.ContextSourceEnum;
import io.shulie.takin.kafka.receiver.dto.web.ApplicationVo;
import io.shulie.takin.kafka.receiver.dto.web.PushMiddlewareListRequest;
import io.shulie.takin.kafka.receiver.dto.web.PushMiddlewareRequest;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.ApplicationMiddleware;
import io.shulie.takin.kafka.receiver.entity.ApplicationMnt;
import io.shulie.takin.kafka.receiver.service.IApplicationMiddlewareService;
import io.shulie.takin.kafka.receiver.service.IApplicationMntService;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(
        SpringJUnit4ClassRunner.class
)
@ContextConfiguration(classes = Application.class)
public class WebKafkaReceiverTest {

    @Resource
    private IApplicationMiddlewareService iApplicationMiddlewareService;
    @Resource
    private IApplicationMntService iApplicationMntService;

    @Test
    public void test_applicationUpload(){
        ApplicationVo applicationVo = new ApplicationVo();
        applicationVo.setApplicationName("unit-test");
        applicationVo.setNodeNum(1);
        applicationVo.setDdlScriptPath("unit-test-ddl.sh");
        applicationVo.setCleanScriptPath("unit-test-clean.sh");
        applicationVo.setReadyScriptPath("unit-test-ready.sh");
        applicationVo.setBasicScriptPath("unit-test-basic.sh");
        applicationVo.setCacheScriptPath("unit-test-cache.sh");
        iApplicationMntService.dealAddApplicationMessage(JSONObject.toJSONString(applicationVo), getTenantInfo());

        QueryWrapper<ApplicationMnt> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApplicationMnt::getApplicationName, "unit-test");
        List<ApplicationMnt> applications = iApplicationMntService.list(queryWrapper);
        //中间件上传必然会有结果
        Assert.assertTrue(CollectionUtils.isNotEmpty(applications));
        //中间件上传之后，数据库只会有一个中间件
        Assert.assertEquals(1, applications.size());
    }

    @Test
    public void test_middlewareUpload() {
        PushMiddlewareRequest request = new PushMiddlewareRequest();
        request.setApplicationName("unit-test");
        List<PushMiddlewareListRequest> middlewareList = new ArrayList<>();
        PushMiddlewareListRequest pushMiddlewareListRequest = new PushMiddlewareListRequest();
        pushMiddlewareListRequest.setArtifactId("jetty-plus");
        pushMiddlewareListRequest.setGroupId("org.eclipse.jetty");
        pushMiddlewareListRequest.setVersion("9.4.25.v20191220");
        middlewareList.add(pushMiddlewareListRequest);
        request.setMiddlewareList(middlewareList);
        iApplicationMiddlewareService.dealMessage(JSONObject.toJSONString(request), getTenantInfo());

        QueryWrapper<ApplicationMiddleware> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApplicationMiddleware::getApplicationName, "unit-test");
        List<ApplicationMiddleware> middlewares = iApplicationMiddlewareService.list(queryWrapper);
        //中间件上传必然会有结果
        Assert.assertTrue(CollectionUtils.isNotEmpty(middlewares));
        //中间件上传之后，数据库只会有一个中间件
        Assert.assertEquals(1, middlewares.size());
    }

    private TenantCommonExt getTenantInfo() {
        TenantCommonExt tenantCommonExt = new TenantCommonExt();
        tenantCommonExt.setTenantId(77L);
        tenantCommonExt.setTenantAppKey("cc60995a-9cf4-4e45-84bb-28891a4dcd8f");
        tenantCommonExt.setEnvCode("test");
        tenantCommonExt.setTenantCode("liantongtest");
        tenantCommonExt.setSource(ContextSourceEnum.AGENT.getCode());
        tenantCommonExt.setUserId(998L);
        return tenantCommonExt;
    }
}
