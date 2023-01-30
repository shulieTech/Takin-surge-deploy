package io.shulie.takin.kafka.receiver.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "io.shulie.takin.kafka.receiver.dao.clickhouse",
        sqlSessionFactoryRef = "clickhouseSqlSessionFactory")
public class ClickhouseDataSourceConfig {

    @Value("${spring.datasource.clickhouse.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.clickhouse.url}")
    private String url;
    @Value("${spring.datasource.clickhouse.username}")
    private String username;
    @Value("${spring.datasource.clickhouse.password}")
    private String password;

    @Bean(name = "clickhouseDataSource")
    public DataSource clickhouseDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setPoolName("clickhouseDataSourcePool");
        return dataSource;
    }

    /**
     * clickhouse数据源
     */
    @Primary
    @Bean(name = "clickhouseSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("clickhouseDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // 设置Mybatis全局配置路径
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:clickhouse/mapper/*.xml"));
        return bean.getObject();
    }

}
