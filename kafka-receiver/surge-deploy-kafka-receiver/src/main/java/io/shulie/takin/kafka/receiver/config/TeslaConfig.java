package io.shulie.takin.kafka.receiver.config;

import com.shulie.tesla.sequence.impl.DefaultSequence;
import com.shulie.tesla.sequence.impl.DefaultSequenceDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author 无涯
 * @date 2020/12/21 5:16 下午
 */
@Configuration
public class TeslaConfig {

    @Bean
    public DefaultSequence baseOrderLineSequence(@Qualifier("webDataSource") DataSource dataSource) {
        DefaultSequenceDao defaultSequenceDao = new DefaultSequenceDao();
        defaultSequenceDao.setDataSource(dataSource);
        defaultSequenceDao.setStep(500);
        defaultSequenceDao.setTableName("t_tc_sequence");
        DefaultSequence defaultSequence = new DefaultSequence();
        defaultSequence.setSequenceDao(defaultSequenceDao);
        defaultSequence.setName("BASE_ORDER_LINE");
        return defaultSequence;
    }
    @Bean
    public DefaultSequence threadOrderLineSequence(@Qualifier("webDataSource") DataSource dataSource) {
        DefaultSequenceDao defaultSequenceDao = new DefaultSequenceDao();
        defaultSequenceDao.setDataSource(dataSource);
        defaultSequenceDao.setStep(500);
        defaultSequenceDao.setTableName("t_tc_sequence");
        DefaultSequence defaultSequence = new DefaultSequence();
        defaultSequence.setSequenceDao(defaultSequenceDao);
        defaultSequence.setName("THREAD_ORDER_LINE");
        return defaultSequence;
    }
}
