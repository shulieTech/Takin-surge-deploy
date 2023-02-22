package io.shulie.takin.kafka.receiver.service.impl;

import java.time.LocalDateTime;

import io.shulie.takin.kafka.receiver.dto.web.ApplicationDsWarnVO;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.ApplicationDsWarn;
import io.shulie.takin.kafka.receiver.dao.web.ApplicationDsWarnMapper;
import io.shulie.takin.kafka.receiver.service.IApplicationDsWarnService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.utils.json.JsonHelper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2023-02-15
 */
@Service
public class ApplicationDsWarnServiceImpl extends ServiceImpl<ApplicationDsWarnMapper, ApplicationDsWarn> implements IApplicationDsWarnService {

    @Resource
    private ApplicationDsWarnMapper applicationDsWarnMapper;

    @Override
    public void dealMessage(String message, TenantCommonExt dealHeader) {
        List<ApplicationDsWarnVO> applicationDsWarnVOs = JsonHelper.json2List(message, ApplicationDsWarnVO.class);
        if (CollectionUtils.isEmpty(applicationDsWarnVOs)) {
            return;
        }
        List<ApplicationDsWarn> applicationDsWarns = applicationDsWarnVOs.stream().map(o -> this.converter(o, dealHeader)).collect(Collectors.toList());
        for (ApplicationDsWarn applicationDsWarn : applicationDsWarns) {
            applicationDsWarnMapper.insertOrUpdate(applicationDsWarn);
        }
    }

    private ApplicationDsWarn converter(ApplicationDsWarnVO applicationDsWarnVO, TenantCommonExt dealHeader) {
        ApplicationDsWarn applicationDsWarn = new ApplicationDsWarn();
        applicationDsWarn.setAppName(applicationDsWarnVO.getAppName());
        applicationDsWarn.setAgentId(applicationDsWarnVO.getAgentId());
        applicationDsWarn.setCheckInterval(applicationDsWarnVO.getCheckInterval());
        applicationDsWarn.setStatus(applicationDsWarnVO.isSuccess() ? 0 : 1);
        applicationDsWarn.setCheckTime(applicationDsWarnVO.getCheckTime());
        if (applicationDsWarnVO.getConfigKey() != null) {
            if (applicationDsWarnVO.getConfigKey().contains("|")) {
                String[] split = applicationDsWarnVO.getConfigKey().split("\\|");
                applicationDsWarn.setCheckUrl(split[0]);
                applicationDsWarn.setCheckUser(split[1]);
            } else {
                applicationDsWarn.setCheckUrl(applicationDsWarnVO.getConfigKey());
            }
        }
        applicationDsWarn.setErrorMsg(applicationDsWarnVO.getErrorMsg());
        applicationDsWarn.setHostIp(applicationDsWarnVO.getHostIp());
        applicationDsWarn.setTenantId(dealHeader.getTenantId());
        applicationDsWarn.setEnvCode(dealHeader.getEnvCode());
        return applicationDsWarn;
    }


}
