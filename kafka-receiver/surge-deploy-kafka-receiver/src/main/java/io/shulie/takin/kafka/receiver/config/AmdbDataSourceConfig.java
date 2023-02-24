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
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "io.shulie.takin.kafka.receiver.dao.amdb",
        sqlSessionFactoryRef = "amdbSqlSessionFactory")
public class AmdbDataSourceConfig {
    @Value("${spring.datasource.amdb.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.amdb.url}")
    private String url;
    @Value("${spring.datasource.amdb.username}")
    private String username;
    @Value("${spring.datasource.amdb.password}")
    private String password;

    @Bean(name = "amdbDataSource")
    public DataSource amdbDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setPoolName("amdbDataSourcePool");
        return dataSource;
    }

    /**
     * amdb数据源
     */
    @Primary
    @Bean(name = "amdbSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("amdbDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // 设置Mybatis全局配置路径
//        bean.setMapperLocations(
//                new PathMatchingResourcePatternResolver().getResources("classpath*:amdb/mapper/*.xml"));
        return bean.getObject();
    }

    @Primary
    @Bean(name = "amdbTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("amdbDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean(name = "amdbSqlSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(
            @Qualifier("amdbSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "amdbTransactionTemplate")
    public TransactionTemplate transactionTemplate (@Qualifier("amdbTransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

}
