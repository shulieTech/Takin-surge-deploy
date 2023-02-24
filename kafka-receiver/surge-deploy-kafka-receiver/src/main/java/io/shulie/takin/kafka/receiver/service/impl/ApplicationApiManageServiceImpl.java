package io.shulie.takin.kafka.receiver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import io.shulie.takin.kafka.receiver.dao.web.ApplicationApiManageMapper;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.ApplicationApiManage;
import io.shulie.takin.kafka.receiver.entity.ApplicationMnt;
import io.shulie.takin.kafka.receiver.service.IApplicationApiManageService;
import io.shulie.takin.kafka.receiver.service.IApplicationMntService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class ApplicationApiManageServiceImpl extends ServiceImpl<ApplicationApiManageMapper, ApplicationApiManage> implements IApplicationApiManageService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationApiManageServiceImpl.class);

    @Resource
    private IApplicationMntService iApplicationMntService;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String EMPTY = " ";


    @Override
    public void dealMessage(Map<String, List<String>> register, TenantCommonExt dealHeader) {

        for (Map.Entry<String, List<String>> entry : register.entrySet()) {
            String appName = String.valueOf(entry.getKey());
            List<ApplicationApiManage> batch = Lists.newArrayList();
            try {
                List<String> apis = entry.getValue();
                if (StringUtils.isBlank(appName) || CollectionUtils.isEmpty(apis)) {
                    continue;
                }

                ApplicationMnt applicationMnt = iApplicationMntService.getApplicationByTenantIdAndName(appName, dealHeader);
                if (applicationMnt == null) {
                    log.warn("入口规则插入数据失败，没有找到对应的应用，跳过当前数据，appName:{},value:{}", appName, JSONObject.toJSONString(entry.getValue()));
                    continue;
                }

                apis.stream().filter(PATH_MATCHER::isPattern).forEach(api -> {
                    String[] str = api.split("#");
                    String requestMethod = str[1];
                    if ("[]".equals(requestMethod)) {
                        //
                        requestMethod = EMPTY;
                    } else {
                        requestMethod = requestMethod.substring(1, requestMethod.length() - 1);
                    }
                    api = str[0];
                    if (requestMethod.contains("||")) {
                        String[] splits = requestMethod.split("\\|\\|");
                        for (String split : splits) {
                            ApplicationApiManage manage = new ApplicationApiManage();
                            manage.setApi(api);
                            manage.setApplicationName(appName);
                            manage.setMethod(split);
                            converter(dealHeader, batch, applicationMnt, manage);
                        }
                    } else {
                        ApplicationApiManage manage = new ApplicationApiManage();
                        manage.setApi(api.trim());
                        manage.setApplicationName(appName);
                        manage.setMethod(requestMethod);
                        converter(dealHeader, batch, applicationMnt, manage);
                    }
                });
                this.saveData(appName, dealHeader, batch);
            } catch (Exception e) {
                log.error("agent 注册 api 异常, ", e);
            }
        }

    }

    public void saveData(String appName, TenantCommonExt dealHeader, List<ApplicationApiManage> batch) {
        // 把旧的记录删除了，新的再添加
        this.deleteByAppName(appName, dealHeader);
        if (CollectionUtils.isNotEmpty(batch)) {
            batch.forEach(this::save);
        }
    }

    public void deleteByAppName(String appName, TenantCommonExt dealHeader) {
        QueryWrapper<ApplicationApiManage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApplicationApiManage::getApplicationName, appName);
        queryWrapper.lambda().eq(ApplicationApiManage::getEnvCode, dealHeader.getEnvCode());
        queryWrapper.lambda().eq(ApplicationApiManage::getTenantId, dealHeader.getTenantId());
        queryWrapper.lambda().eq(ApplicationApiManage::getIsAgentRegiste, 1);
        List<ApplicationApiManage> applicationApiManages = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(applicationApiManages)) {
            return;
        }
        List<Long> ids = applicationApiManages.stream().map(ApplicationApiManage::getId).collect(Collectors.toList());
        this.removeByIds(ids);
    }

    private void converter(TenantCommonExt dealHeader, List<ApplicationApiManage> batch, ApplicationMnt applicationMnt, ApplicationApiManage manage) {
        manage.setIsDeleted(0);
        manage.setCreateTime(LocalDateTime.now());
        manage.setUpdateTime(LocalDateTime.now());
        manage.setApplicationId(applicationMnt.getApplicationId());
        manage.setUserId(applicationMnt.getUserId());
        manage.setIsAgentRegiste(1);
        manage.setTenantId(dealHeader.getTenantId());
        manage.setEnvCode(dealHeader.getEnvCode());
        manage.setDeptId(applicationMnt.getDeptId());
        batch.add(manage);
    }
}
