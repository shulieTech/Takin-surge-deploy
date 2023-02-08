package io.shulie.takin.kafka.receiver.config;

import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
import io.shulie.surge.data.sink.clickhouse.ClickHouseSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClickhouseConfig {

    @Bean
    public ClickHouseSupport clickHouseSupport(@Value("${config.clickhouse.url}") String clickhouseUrl,
                                               @Value("${config.clickhouse.username}") String clickhouseUserName,
                                               @Value("${config.clickhouse.password}") String clickhousePassword) {
        return new ClickHouseSupport(clickhouseUrl, clickhouseUserName, clickhousePassword, 200, false);
    }

    @Bean
    public ClickHouseShardSupport clickHouseShardSupport(@Value("${config.clickhouse.url}") String clickhouseUrl,
                                                    @Value("${config.clickhouse.username}") String clickhouseUserName,
                                                    @Value("${config.clickhouse.password}") String clickhousePassword) {
        return new ClickHouseShardSupport(clickhouseUrl, clickhouseUserName, clickhousePassword, 200, false);
    }

}
