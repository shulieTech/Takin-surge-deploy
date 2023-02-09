package io.shulie.takin.kafka.receiver.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "io.shulie.takin.kafka.receiver.dao.cloud",
        sqlSessionFactoryRef = "cloudSqlSessionFactory")
public class CloudDataSourceConfig {
    @Value("${spring.datasource.cloud.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.cloud.url}")
    private String url;
    @Value("${spring.datasource.cloud.username}")
    private String username;
    @Value("${spring.datasource.cloud.password}")
    private String password;

    @Bean(name = "cloudDataSource")
    public DataSource cloudDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setPoolName("cloudDataSourcePool");
        return dataSource;
    }

    /**
     * cloud数据源
     */
    @Bean(name = "cloudSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("cloudDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // 设置Mybatis全局配置路径
//        bean.setMapperLocations(
//                new PathMatchingResourcePatternResolver().getResources("classpath*:cloud/mapper/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "cloudTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("cloudDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "cloudSqlSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(
            @Qualifier("cloudSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
