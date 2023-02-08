package io.shulie.takin.kafka.receiver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.shulie.takin.kafka.receiver.dao.web.ReportMapper;
import io.shulie.takin.kafka.receiver.entity.Report;
import io.shulie.takin.kafka.receiver.service.IReportService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhaoyong
 * @since 2023-01-12
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

}
