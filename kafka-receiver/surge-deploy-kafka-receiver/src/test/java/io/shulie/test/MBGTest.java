package io.shulie.test;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.GeneratorBuilder;
import com.baomidou.mybatisplus.generator.config.querys.ClickHouseQuery;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.junit.Test;

public class MBGTest {
//
//    @Test
//    public void test(){
//
//        AutoGenerator mbg = new AutoGenerator();
//        // 全局配置
//        GlobalConfig gc = new GlobalConfig();
//        String projectPath = System.getProperty("user.dir");
//        gc.setOutputDir(projectPath + "/src/main/java");
//        gc.setAuthor("zhaoyong");
//        gc.setOpen(false);
//        gc.setDateType(DateType.TIME_PACK);
//        gc.setSwagger2(false);
//        gc.setIdType(IdType.AUTO);
//        mbg.setGlobalConfig(gc);
//        // 数据源配置
//        DataSourceConfig dsc = new DataSourceConfig();
////        dsc.setUrl("jdbc:mysql://192.168.1.112:3306/trodb?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true&serverTimezone=Asia/Shanghai");
//        dsc.setUrl("jdbc:clickhouse://192.168.1.129:8123/default?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true&serverTimezone=Asia/Shanghai");
//        dsc.setDriverName("ru.yandex.clickhouse.ClickHouseDriver");
//        dsc.setUsername("default");
//        dsc.setPassword("rU4zGjA/");
//        dsc.setDbType(DbType.CLICK_HOUSE);
//        mbg.setDataSource(dsc);
//        // 包配置
//        PackageConfig pc = new PackageConfig();
//        pc.setParent("io.shulie.takin.kafka.receiver").setMapper("dao").setXml("dao.mapper");
//        mbg.setPackageInfo(pc);
//        //策略 设置驼峰命名
//        final StrategyConfig sc = new StrategyConfig();
//        sc.setInclude("t_engine_metrics");
//        sc.setTablePrefix("t_");
//        sc.setNaming(NamingStrategy.underline_to_camel);
//        sc.setColumnNaming(NamingStrategy.underline_to_camel);
//        sc.setRestControllerStyle(false);
//        sc.setControllerMappingHyphenStyle(false);
//        mbg.setStrategy(sc);
//
//        mbg.execute();
//    }


    @Test
    public void test_02(){
        // 获取项目路径
        String projectPath = System.getProperty("user.dir");
        // 全局配置
        GlobalConfig gc = GeneratorBuilder.globalConfigBuilder()
                .fileOverride().openDir(false)
                .outputDir(projectPath + "/src/main/java")
                .author("zhaoyong")
                .enableSwagger()
                .commentDate("yyyy-MM-dd").build();


        // 数据源配置
//        DataSourceConfig dsc = new DataSourceConfig.Builder("jdbc:clickhouse://192.168.1.129:8123", "default", "rU4zGjA/")
//                .dbQuery(new ClickHouseQuery()).schema("default").build();
        DataSourceConfig dsc = new DataSourceConfig.Builder("jdbc:mysql://192.168.1.129:3306/trodb_combine_225","root", "shulie@2020")
                .dbQuery(new MySqlQuery()).build();


        // 包配置
        PackageConfig pc = GeneratorBuilder.packageConfigBuilder().parent("io.shulie.takin.kafka.receiver").build();

        // 策略配置
        StrategyConfig strategy = GeneratorBuilder.strategyConfigBuilder()
                .addInclude("t_scene_manage")
                .addTablePrefix("t_")
                .controllerBuilder().enableHyphenStyle()
                .entityBuilder()
                .naming(NamingStrategy.underline_to_camel)
                .columnNaming(NamingStrategy.underline_to_camel)
                .versionColumnName("version").logicDeleteColumnName("isDelete")
                .enableLombok()
                .build();

        TemplateConfig templateConfig = GeneratorBuilder.templateConfigBuilder().build();

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator(dsc).global(gc).strategy(strategy).template(templateConfig).packageInfo(pc);

        mpg.execute(new FreemarkerTemplateEngine());
    }

}
