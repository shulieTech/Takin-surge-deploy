package io.shulie.takin.kafka.receiver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.constant.web.JobEnum;
import io.shulie.takin.kafka.receiver.dao.web.ShadowJobConfigMapper;
import io.shulie.takin.kafka.receiver.dto.web.ShadowJobConfigQuery;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.ShadowJobConfig;
import io.shulie.takin.kafka.receiver.service.IShadowJobConfigService;
import io.shulie.takin.kafka.receiver.util.XmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 影子JOB任务配置 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class ShadowJobConfigServiceImpl extends ServiceImpl<ShadowJobConfigMapper, ShadowJobConfig> implements IShadowJobConfigService {

    private static final Logger log = LoggerFactory.getLogger(ShadowJobConfigServiceImpl.class);

    @Override
    public void dealMessage(String body, TenantCommonExt dealHeader) {
        if (StringUtils.isBlank(body)) {
            log.warn("接收到数据为空");
            return;
        }
        ShadowJobConfigQuery query = JSONObject.parseObject(body, ShadowJobConfigQuery.class);
        if (query == null || query.getId() == null) {
            log.warn("获取到的请求数据没有id，不进行处理，数据为:{}", body);
            return;
        }
        ShadowJobConfig updateShadowJobConfig = new ShadowJobConfig();
        ShadowJobConfig shadowJobConfig = this.getById(query.getId());
        if (null == shadowJobConfig) {
            log.warn("没有根据id获取到影子job数据，不进行处理，数据为:{}", body);
            return;
        }
        try {
            if (StringUtils.isNotBlank(query.getConfigCode())) {

                if (null != query.getConfigCode() && !query.getConfigCode().equals(shadowJobConfig.getConfigCode())) {
                    updateShadowJobConfig.setConfigCode(query.getConfigCode());
                    Map<String, String> xmlMap = XmlUtil.readStringXml(query.getConfigCode());
                    String className = xmlMap.get("className");
                    String jobType = xmlMap.get("jobType");
                    JobEnum jobText = JobEnum.getJobByText(jobType);

                    //是否有重复
                    // 重复判断
                    ShadowJobConfig shadowJobCreateParam = new ShadowJobConfig();
                    shadowJobCreateParam.setName(className);
                    shadowJobCreateParam.setApplicationId(shadowJobConfig.getApplicationId());
                    shadowJobCreateParam.setType(jobText.ordinal());
                    shadowJobCreateParam.setId(shadowJobConfig.getId());
                    if (this.exist(shadowJobCreateParam)) {
                        log.info(shadowJobCreateParam.getName() + ",类型为" +
                                JobEnum.getJobByIndex(shadowJobCreateParam.getType()).getText() + "已存在");
                        return;
                    }

                    if (StringUtils.isNotBlank(className) && !className.equals(shadowJobConfig.getName())) {
                        updateShadowJobConfig.setName(className);
                    }

                    if (!Integer.valueOf(jobText.ordinal()).equals(shadowJobConfig.getType())) {
                        updateShadowJobConfig.setType(jobText.ordinal());
                    }
                }
            }
            updateShadowJobConfig.setId(query.getId());
            if (null != query.getStatus() && !query.getStatus().equals(shadowJobConfig.getStatus())) {
                updateShadowJobConfig.setStatus(query.getStatus());
            }
            if (null != query.getActive() && !query.getActive().equals(shadowJobConfig.getActive())) {
                updateShadowJobConfig.setActive(query.getActive());
            }

            if (StringUtils.isNotBlank(updateShadowJobConfig.getConfigCode()) || null != query.getStatus()
                    || null != query.getActive()) {
                this.updateById(updateShadowJobConfig);
            }
            // 仅仅更新备注字段
            if (query.getRemark() != null) {
                ShadowJobConfig updateRemark = new ShadowJobConfig();
                updateRemark.setId(query.getId());
                updateRemark.setRemark(query.getRemark());
                this.updateById(updateRemark);
            }
            //去掉了同步zk和同步nacos的逻辑，因为agent发出来的消息，只会更新备注和状态
        } catch (Exception e) {
            log.error("处理影子job更新数据异常，data为:{}", body, e);
        }
    }

    private boolean exist(ShadowJobConfig param) {
        LambdaQueryWrapper<ShadowJobConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShadowJobConfig::getApplicationId, param.getApplicationId());
        queryWrapper.eq(ShadowJobConfig::getType, param.getType());
        queryWrapper.eq(ShadowJobConfig::getName, param.getName());
        if(param.getId() != null ) {
            queryWrapper.ne(ShadowJobConfig::getId, param.getId());
        }
        queryWrapper.eq(ShadowJobConfig::getIsDeleted, false);
        return this.count(queryWrapper) > 0;
    }
}
