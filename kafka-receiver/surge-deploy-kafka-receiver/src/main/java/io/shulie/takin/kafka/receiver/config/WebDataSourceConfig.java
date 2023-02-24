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
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "io.shulie.takin.kafka.receiver.dao.web",
        sqlSessionFactoryRef = "webSqlSessionFactory")
public class WebDataSourceConfig {
    @Value("${spring.datasource.web.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.web.url}")
    private String url;
    @Value("${spring.datasource.web.username}")
    private String username;
    @Value("${spring.datasource.web.password}")
    private String password;

    @Bean(name = "webDataSource")
    public DataSource webDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setPoolName("webDataSourcePool");
        return dataSource;
    }

    /**
     * web数据源
     */
    @Bean(name = "webSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("webDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // 设置Mybatis全局配置路径
//        bean.setMapperLocations(
//                new PathMatchingResourcePatternResolver().getResources("classpath*:web/mapper/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "webTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("webDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "webSqlSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(
            @Qualifier("webSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "webTransactionTemplate")
    public TransactionTemplate transactionTemplate (@Qualifier("webTransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
