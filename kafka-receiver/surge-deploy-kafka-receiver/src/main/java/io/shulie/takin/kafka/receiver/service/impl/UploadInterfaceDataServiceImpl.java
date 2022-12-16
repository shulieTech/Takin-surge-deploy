package io.shulie.takin.kafka.receiver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.shulie.takin.kafka.receiver.constant.web.Constants;
import io.shulie.takin.kafka.receiver.dto.web.TUploadInterfaceDetailVo;
import io.shulie.takin.kafka.receiver.dto.web.TUploadInterfaceVo;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.UploadInterfaceData;
import io.shulie.takin.kafka.receiver.dao.web.UploadInterfaceDataMapper;
import io.shulie.takin.kafka.receiver.service.IUploadInterfaceDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.service.Snowflake;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * dubbo和job接口上传收集表 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-08
 */
@Service
public class UploadInterfaceDataServiceImpl extends ServiceImpl<UploadInterfaceDataMapper, UploadInterfaceData> implements IUploadInterfaceDataService, InitializingBean {

    @Value("${node.num:1}")
    private String nodeNum;

    private Snowflake snowflake;

    @Override
    public void dealMessage(String message, TenantCommonExt dealHeader) {
        if (StringUtils.isBlank(message)){
            return;
        }
        TUploadInterfaceVo uploadInterfaceVo = JSONObject.parseObject(message, TUploadInterfaceVo.class);
        if (uploadInterfaceVo == null || StringUtils.isEmpty(uploadInterfaceVo.getAppName()) || CollectionUtils.isEmpty(
                uploadInterfaceVo.getAppDetails())) {
            return;
        }

        //保存前先删除数据
        deleteByAppName(uploadInterfaceVo.getAppName(), dealHeader);

        List<UploadInterfaceData> saveDataList = new ArrayList<>();
        for (TUploadInterfaceDetailVo appDetail : uploadInterfaceVo.getAppDetails()) {
            UploadInterfaceData tempDate = new UploadInterfaceData();
            tempDate.setAppName(uploadInterfaceVo.getAppName());
            tempDate.setId(snowflake.next());
            tempDate.setInterfaceValue(appDetail.getInterfaceName());
            tempDate.setEnvCode(dealHeader.getEnvCode());
            tempDate.setTenantId(dealHeader.getTenantId());
            if (Constants.UPLOAD_DATA_TYPE_DUBBO.equalsIgnoreCase(appDetail.getType())) {
                tempDate.setInterfaceType(Constants.UPLOAD_DATA_DBTYPE_DUBBO);
            } else if (Constants.UPLOAD_DATA_TYPE_JOB.equalsIgnoreCase(appDetail.getType())) {
                tempDate.setInterfaceType(Constants.UPLOAD_DATA_DBTYPE_JOB);
            } else {
                continue;
            }
            saveDataList.add(tempDate);
            if (saveDataList.size() > 500) {
                this.saveBatch(saveDataList);
                saveDataList = new ArrayList<>();
            }
        }
        if (CollectionUtils.isNotEmpty(saveDataList)) {
            this.saveBatch(saveDataList);
        }
    }

    private void deleteByAppName(String appName, TenantCommonExt dealHeader) {
        QueryWrapper<UploadInterfaceData> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UploadInterfaceData::getAppName, appName);
        queryWrapper.lambda().eq(UploadInterfaceData::getEnvCode, dealHeader.getEnvCode());
        queryWrapper.lambda().eq(UploadInterfaceData::getTenantId, dealHeader.getTenantId());
        List<UploadInterfaceData> uploadInterfaceData = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(uploadInterfaceData)){
            return;
        }
        List<Long> longList = uploadInterfaceData.stream().map(UploadInterfaceData::getId).collect(Collectors.toList());
        this.removeByIds(longList);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        snowflake = new Snowflake(Integer.parseInt(nodeNum));
    }
}
