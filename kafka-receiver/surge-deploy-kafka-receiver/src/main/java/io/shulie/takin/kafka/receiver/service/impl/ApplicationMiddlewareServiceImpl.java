package io.shulie.takin.kafka.receiver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.web.ApplicationMiddlewareMapper;
import io.shulie.takin.kafka.receiver.dto.web.PushMiddlewareListRequest;
import io.shulie.takin.kafka.receiver.dto.web.PushMiddlewareRequest;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.ApplicationMiddleware;
import io.shulie.takin.kafka.receiver.entity.ApplicationMnt;
import io.shulie.takin.kafka.receiver.service.IApplicationMiddlewareService;
import io.shulie.takin.kafka.receiver.service.IApplicationMntService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 应用中间件 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class ApplicationMiddlewareServiceImpl extends ServiceImpl<ApplicationMiddlewareMapper, ApplicationMiddleware> implements IApplicationMiddlewareService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMiddlewareServiceImpl.class);
    private final Map<String, Long> applicationTimeMap = new HashMap<>();

    @Resource
    private IApplicationMntService iApplicationMntService;

    @Override
    public void dealMessage(String body, TenantCommonExt dealHeader) {
        if (StringUtils.isBlank(body)) {
            return;
        }
        PushMiddlewareRequest pushMiddlewareRequest = JSONObject.parseObject(body, PushMiddlewareRequest.class);
        List<PushMiddlewareListRequest> middlewareList = pushMiddlewareRequest.getMiddlewareList();
        if (middlewareList.isEmpty()) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        String applicationName = pushMiddlewareRequest.getApplicationName();
        log.info("应用中间件上报 --> 应用名称: {}", applicationName);
        if (applicationTimeMap.containsKey(applicationName) && currentTimeMillis - applicationTimeMap.get(applicationName) > 60 * 1000) {
            // 60s 内, 同一个应用重复上传的, 不处理, 幂等
            return;
        }
        applicationTimeMap.put(applicationName, currentTimeMillis);

        // 根据中间件名称查询, 获取应用id
        ApplicationMnt application = iApplicationMntService.getApplicationByTenantIdAndName(applicationName, dealHeader);
        try {
            if (application == null) {
                log.error("应用中间件上报, 应用不存在！" + body);
                return;
            }

            // 根据应用id, 删除应用中间件
            log.info("应用中间件上报 --> 删除应用下原有的中间件");
            this.removeByApplicationId(application.getApplicationId(), dealHeader);

            // 新的中间件插入
            log.info("应用中间件上报 --> 插入上报中间件");
            List<ApplicationMiddleware> createApplicationMiddlewareParamList =
                    this.listCreateApplicationMiddlewareParam(middlewareList, application, dealHeader);
            createApplicationMiddlewareParamList.forEach(this::save);
        } catch (Exception e) {
            log.error("处理应用中间件数据出现异常", e);
        }
    }

    private void removeByApplicationId(Long applicationId, TenantCommonExt dealHeader) {
        QueryWrapper<ApplicationMiddleware> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApplicationMiddleware::getApplicationId, applicationId);
        queryWrapper.lambda().eq(ApplicationMiddleware::getTenantId, dealHeader.getTenantId());
        queryWrapper.lambda().eq(ApplicationMiddleware::getEnvCode, dealHeader.getEnvCode());
        List<ApplicationMiddleware> applicationMiddlewares = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(applicationMiddlewares)) {
            return;
        }
        List<Long> ids = applicationMiddlewares.stream().map(ApplicationMiddleware::getId).collect(Collectors.toList());
        this.removeByIds(ids);
    }

    private List<ApplicationMiddleware> listCreateApplicationMiddlewareParam(
            List<PushMiddlewareListRequest> middlewareList, ApplicationMnt application, TenantCommonExt dealHeader) {
        return middlewareList.stream().map(pushMiddlewareListRequest -> {
            ApplicationMiddleware createParam = new ApplicationMiddleware();
            createParam.setApplicationId(application.getApplicationId());
            createParam.setApplicationName(application.getApplicationName());

            String artifactId = pushMiddlewareListRequest.getArtifactId();
            createParam.setArtifactId(artifactId == null ? "" : artifactId);

            String groupId = pushMiddlewareListRequest.getGroupId();
            createParam.setGroupId(groupId == null ? "" : groupId);

            String version = pushMiddlewareListRequest.getVersion();
            createParam.setVersion(version == null ? "" : version);

            createParam.setEnvCode(dealHeader.getEnvCode());
            createParam.setTenantId(dealHeader.getTenantId());
            return createParam;
        }).collect(Collectors.toList());
    }
}
