package io.shulie.takin.kafka.receiver.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.web.AppAgentConfigReportMapper;
import io.shulie.takin.kafka.receiver.dto.web.ConfigReportInputParam;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.AppAgentConfigReport;
import io.shulie.takin.kafka.receiver.entity.ApplicationMnt;
import io.shulie.takin.kafka.receiver.service.IAppAgentConfigReportService;
import io.shulie.takin.kafka.receiver.service.IApplicationMntService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * agent配置上报详情 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class AppAgentConfigReportServiceImpl extends ServiceImpl<AppAgentConfigReportMapper, AppAgentConfigReport> implements IAppAgentConfigReportService {

    private static final Logger log = LoggerFactory.getLogger(AppAgentConfigReportServiceImpl.class);

    @Resource
    private IApplicationMntService iApplicationMntService;

    @Override
    public void dealMessage(String body, TenantCommonExt dealHeader) {
        if (StringUtils.isBlank(body)) {
            return;
        }
        ConfigReportInputParam inputParam = JSONObject.parseObject(body, ConfigReportInputParam.class);
        if (inputParam == null) {
            log.error("uploadConfigInfo 上传参数不能为空");
            return;
        }
        ApplicationMnt application = iApplicationMntService.getApplicationByTenantIdAndName(inputParam.getApplicationName(), dealHeader);
        if (application == null || Objects.isNull(application.getApplicationId())) {
            log.error("uploadConfigInfo 应用【{}】不存在", inputParam.getApplicationName());
            return;
        }

        List<AppAgentConfigReport> saveList = new ArrayList<>();

        List<ConfigReportInputParam.ConfigInfo> globalConf = inputParam.getGlobalConf();
        List<ConfigReportInputParam.ConfigInfo> appConf = inputParam.getAppConf();
        List<ConfigReportInputParam.ConfigInfo> newConfigInfo = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(appConf)) {
            newConfigInfo.addAll(appConf);
        }
        if (CollectionUtils.isNotEmpty(globalConf)) {
            newConfigInfo.addAll(globalConf);
        }
        if (CollectionUtils.isNotEmpty(newConfigInfo)) {
            newConfigInfo.forEach(global -> {
                AppAgentConfigReport reportParam = new AppAgentConfigReport();
                reportParam.setAgentId(inputParam.getAgentId());
                reportParam.setApplicationId(application.getApplicationId());
                reportParam.setApplicationName(inputParam.getApplicationName());
                reportParam.setConfigType(global.getBizType());
                reportParam.setConfigKey(global.getKey());
                reportParam.setConfigValue(global.getValue());
                reportParam.setTenantId(dealHeader.getTenantId());
                reportParam.setEnvCode(dealHeader.getEnvCode());
                reportParam.setUserId(dealHeader.getUserId());
                saveList.add(reportParam);
            });
        }
        List<AppAgentConfigReport> dbResults = this.listByAppId(application.getApplicationId(), dealHeader);

        Map<String, Map<Integer, List<AppAgentConfigReport>>> dbMap = CollStreamUtil.groupBy2Key(dbResults,
                AppAgentConfigReport::getAgentId, AppAgentConfigReport::getConfigType);

        Map<String, Map<Integer, List<AppAgentConfigReport>>> reportMap = CollStreamUtil.groupBy2Key(saveList,
                AppAgentConfigReport::getAgentId, AppAgentConfigReport::getConfigType);

        List<AppAgentConfigReport> updateList = new ArrayList<>();

        dbMap.forEach((agentId, report2TypeForDbMap) -> {
            if (!reportMap.containsKey(agentId)) {
                return;
            }
            Map<Integer, List<AppAgentConfigReport>> report2TypeForReportMap = reportMap.get(agentId);

            report2TypeForDbMap.forEach((configType, reports) -> {
                if (!report2TypeForReportMap.containsKey(configType)) {
                    return;
                }
                Map<String, AppAgentConfigReport> report2KeyForParamsMap = report2TypeForReportMap.get(configType)
                        .stream().collect(Collectors.toMap(AppAgentConfigReport::getConfigKey, Function.identity()));

                Map<String, AppAgentConfigReport> report2KeyForDbMap = reports.stream()
                        .collect(Collectors.toMap(AppAgentConfigReport::getConfigKey, Function.identity()));

                report2KeyForDbMap.forEach((configKey, data) -> {
                    if (!report2KeyForParamsMap.containsKey(configKey)) {
                        return;
                    }
                    AppAgentConfigReport removeObj = report2KeyForParamsMap.get(configKey);
                    saveList.remove(removeObj);
                    AppAgentConfigReport convert = Convert.convert(AppAgentConfigReport.class, removeObj);
                    convert.setId(data.getId());
                    updateList.add(convert);

                });
            });
        });
        saveList.forEach(this::save);
        updateList.forEach(this::updateById);
    }

    private List<AppAgentConfigReport> listByAppId(Long applicationId, TenantCommonExt dealHeader) {
        QueryWrapper<AppAgentConfigReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppAgentConfigReport::getApplicationId, applicationId);
        queryWrapper.lambda().eq(AppAgentConfigReport::getIsDeleted, 0);
        queryWrapper.lambda().eq(AppAgentConfigReport::getTenantId, dealHeader.getTenantId());
        queryWrapper.lambda().eq(AppAgentConfigReport::getEnvCode, dealHeader.getEnvCode());
        return this.list(queryWrapper);
    }
}
