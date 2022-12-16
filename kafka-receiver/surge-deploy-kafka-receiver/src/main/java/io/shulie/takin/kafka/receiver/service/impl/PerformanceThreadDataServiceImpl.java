package io.shulie.takin.kafka.receiver.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shulie.tesla.sequence.impl.DefaultSequence;
import io.shulie.takin.kafka.receiver.constant.web.SceneManageStatusEnum;
import io.shulie.takin.kafka.receiver.dto.web.PerformanceBaseDataReq;
import io.shulie.takin.kafka.receiver.dto.web.PerformanceThreadDataVO;
import io.shulie.takin.kafka.receiver.dto.web.TenantCommonExt;
import io.shulie.takin.kafka.receiver.entity.*;
import io.shulie.takin.kafka.receiver.dao.web.PerformanceThreadDataMapper;
import io.shulie.takin.kafka.receiver.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.util.InfluxDatabaseWriter;
import io.shulie.takin.utils.json.JsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2022-12-09
 */
@Service
public class PerformanceThreadDataServiceImpl extends ServiceImpl<PerformanceThreadDataMapper, PerformanceThreadData> implements IPerformanceThreadDataService {
    private static final Logger log = LoggerFactory.getLogger(PerformanceThreadDataServiceImpl.class);
    private static final AtomicInteger INTEGER = new AtomicInteger();

    private final static String DEFAULT_THREAD_STATUS = "RUNNABLE";

    @Value("${performance.base.agent.frequency: 100}")
    private String performanceBaseAgentFrequency;

    @Resource
    private ISceneManageService iSceneManageService;
    @Resource
    private ISceneBusinessActivityRefService iSceneBusinessActivityRefService;
    @Resource
    private IApplicationMntService iApplicationMntService;
    @Resource
    private DefaultSequence baseOrderLineSequence;
    @Resource
    private DefaultSequence threadOrderLineSequence;
    @Resource
    private InfluxDatabaseWriter influxDatabaseWriter;
    @Resource
    private IPerformanceThreadStackDataService iPerformanceThreadStackDataService;

    @Override
    public void dealMessage(String message, TenantCommonExt dealHeader) {
        if (INTEGER.get() > 100000000) {
            INTEGER.set(0);
        }
        int frequency = Integer.parseInt(performanceBaseAgentFrequency);
        if (INTEGER.getAndIncrement() % frequency != 0) {
            return;
        }
        List<ApplicationMnt> workingApplications = getWorkingApplications(dealHeader);
        if (CollectionUtil.isEmpty(workingApplications)) {
            return;
        }
        List<String> applicationNames = workingApplications.stream().map(ApplicationMnt::getApplicationName).collect(Collectors.toList());
        PerformanceBaseDataReq performanceBaseDataReq = JSONObject.parseObject(message, PerformanceBaseDataReq.class);
        if (!applicationNames.contains(performanceBaseDataReq.getAppName())) {
            return;
        }
        long baseId = baseOrderLineSequence.nextValue();
        // 插入base数据
        influxWriterBase(performanceBaseDataReq, baseId, dealHeader);

        influxWriterThread(performanceBaseDataReq, baseId, dealHeader);
    }


    private void influxWriterBase(PerformanceBaseDataReq param, Long baseId, TenantCommonExt dealHeader) {
        long start = System.currentTimeMillis();
        // 计算合计cpu利用率
        double cpuUseRate = 0.00;
        for (PerformanceThreadDataVO dataParam : param.getThreadDataList()) {
            BigDecimal b1 = BigDecimal.valueOf((cpuUseRate));
            BigDecimal b2 = BigDecimal.valueOf(dataParam.getThreadCpuUsage() == null ? 0.00 : dataParam.getThreadCpuUsage());
            cpuUseRate = b1.add(b2).doubleValue();
        }
        Map<String, Object> fields = Maps.newHashMap();
        fields.put("total_memory", param.getTotalMemory());
        fields.put("perm_memory", param.getPermMemory());
        fields.put("young_memory", param.getYoungMemory());
        fields.put("old_memory", param.getOldMemory());
        fields.put("young_gc_count", param.getYoungGcCount());
        fields.put("full_gc_count", param.getFullGcCount());
        fields.put("young_gc_cost", param.getYoungGcCost());
        fields.put("full_gc_cost", param.getFullGcCost());
        fields.put("cpu_use_rate", cpuUseRate);
        // 新增buffer
        fields.put("total_buffer_pool_memory", param.getTotalBufferPoolMemory());
        // 新增非堆
        fields.put("total_no_heap_memory", param.getTotalNonHeapMemory());
        fields.put("thread_count", param.getThreadDataList().size());
        // 保存原始时间戳，后续作为组装base_id的唯一值
        fields.put("timestamp", param.getTimestamp() != null ? param.getTimestamp() : "null");
        // base_id 先存进去
        fields.put("base_id", baseId);
        Map<String, String> tags = Maps.newHashMap();
        tags.put("agent_id", StringUtils.isNotBlank(param.getAgentId()) ? param.getAgentId() : "null");
        tags.put("app_name", StringUtils.isNotBlank(param.getAppName()) ? param.getAppName() : "null");
        tags.put("app_ip", StringUtils.isNotBlank(param.getAppIp()) ? param.getAppIp() : "null");
        tags.put("process_id", param.getProcessId() != null ? String.valueOf(param.getProcessId()) : "null");
        tags.put("process_name", StringUtils.isNotBlank(param.getProcessName()) ? param.getProcessName() : "null");
        // 租户信息tag
        tags.put("tenant_id", dealHeader.getTenantId() + "");
        tags.put("tenant_app_key", dealHeader.getTenantAppKey());
        tags.put("env_code", dealHeader.getEnvCode());
        try {
            influxDatabaseWriter.insert("t_performance_base_data", tags, fields, param.getTimestamp());
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("influxWriterBase运行时间：{}", System.currentTimeMillis() - start);

    }

    private void influxWriterThread(PerformanceBaseDataReq param, Long baseId, TenantCommonExt dealHeader) {
        long start = System.currentTimeMillis();
        // 记录关联关系 thread threadStack
        List<PerformanceThreadStackData> stackDataEntities = Lists.newArrayList();
        param.getThreadDataList().forEach(data -> {
            if (StringUtils.isBlank(data.getThreadStatus())) {
                data.setThreadStatus(DEFAULT_THREAD_STATUS);
            }
            long threadId = threadOrderLineSequence.nextValue();
            // 记录关联关系
            PerformanceThreadStackData entity = new PerformanceThreadStackData();
            entity.setThreadStackLink(threadId);
            entity.setThreadStack(data.getThreadStack());
            entity.setGmtCreate(LocalDateTime.now());
            stackDataEntities.add(entity);
            // 处理数据
            data.setThreadStack("");
            data.setThreadStackLink(threadId);
        });
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        PerformanceThreadData threadDataEntity = new PerformanceThreadData();
        threadDataEntity.setBaseId(baseId);
        threadDataEntity.setAgentId(StringUtils.isNotBlank(param.getAgentId()) ? param.getAgentId() : "null");
        threadDataEntity.setAppIp(StringUtils.isNotBlank(param.getAppIp()) ? param.getAppIp() : "null");
        threadDataEntity.setAppName(StringUtils.isNotBlank(param.getAppName()) ? param.getAppName() : "null");
        threadDataEntity.setTimestamp(param.getTimestamp() != null ? simpleDateFormat.format(new Date(param.getTimestamp())) : "null");
        threadDataEntity.setThreadData(JsonHelper.bean2Json(param.getThreadDataList()));
        threadDataEntity.setGmtCreate(LocalDateTime.now());
        this.save(threadDataEntity);
        // 插入influxdb
        long mid = System.currentTimeMillis();
        // threadStack 存入mysql thread_stack_link
        if (CollectionUtil.isEmpty(stackDataEntities)) {
            return;
        }
        if (stackDataEntities.size() > 40) {
            for (List<PerformanceThreadStackData> entityList : ListUtil.split(stackDataEntities, 40)) {
                iPerformanceThreadStackDataService.saveBatch(entityList);
            }
        } else {
            iPerformanceThreadStackDataService.saveBatch(stackDataEntities);
        }
        log.debug("influxDBWriter运行时间：{},insertBatchSomeColumn运行时间:{},数据量:{}", mid - start, System.currentTimeMillis() - mid, stackDataEntities.size());
    }

    private List<ApplicationMnt> getWorkingApplications(TenantCommonExt dealHeader) {
        List<Integer> workingStatus = SceneManageStatusEnum.getWorking().stream().map(SceneManageStatusEnum::getValue).collect(Collectors.toList());
        QueryWrapper<SceneManage> sceneManageQueryWrapper = new QueryWrapper<>();
        sceneManageQueryWrapper.lambda().eq(SceneManage::getIsDeleted, 0);
        sceneManageQueryWrapper.lambda().eq(SceneManage::getTenantId, dealHeader.getTenantId());
        sceneManageQueryWrapper.lambda().eq(SceneManage::getEnvCode, dealHeader.getEnvCode());
        sceneManageQueryWrapper.lambda().in(SceneManage::getStatus, workingStatus);
        List<SceneManage> workingSceneManages = iSceneManageService.list(sceneManageQueryWrapper);
        if (CollectionUtil.isEmpty(workingSceneManages)) {
            return null;
        }
        List<Long> workingSceneManageIds = workingSceneManages.stream().map(SceneManage::getId).collect(Collectors.toList());
        QueryWrapper<SceneBusinessActivityRef> sceneBusinessActivityRefQueryWrapper = new QueryWrapper<>();
        sceneBusinessActivityRefQueryWrapper.lambda().in(SceneBusinessActivityRef::getSceneId, workingSceneManageIds);
        sceneBusinessActivityRefQueryWrapper.lambda().eq(SceneBusinessActivityRef::getIsDeleted, 0);
        List<SceneBusinessActivityRef> sceneBusinessActivityRefs = iSceneBusinessActivityRefService.list(sceneBusinessActivityRefQueryWrapper);
        if (CollectionUtil.isEmpty(sceneBusinessActivityRefs)) {
            return null;
        }

        List<Long> applicationIds = sceneBusinessActivityRefs.stream()
                .map(SceneBusinessActivityRef::getApplicationIds).filter(StringUtils::isNotEmpty)
                .flatMap(appIds -> Arrays.stream(appIds.split(","))
                        .map(Long::valueOf)).filter(data -> data > 0L).distinct().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(applicationIds)) {
            return null;
        }

        QueryWrapper<ApplicationMnt> applicationMntQueryWrapper = new QueryWrapper<>();
        applicationMntQueryWrapper.lambda().in(ApplicationMnt::getApplicationId, applicationIds);
        return iApplicationMntService.list(applicationMntQueryWrapper);
    }
}
