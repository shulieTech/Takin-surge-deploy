package io.shulie.test;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import org.junit.Test;

public class MBGTest {

    @Test
    public void test(){

        AutoGenerator mbg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor("zhaoyong");
        gc.setOpen(false);
        gc.setDateType(DateType.TIME_PACK);
        gc.setSwagger2(true);
        gc.setIdType(IdType.AUTO);
        mbg.setGlobalConfig(gc);
        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
//        dsc.setUrl("jdbc:mysql://192.168.1.112:3306/trodb?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true&serverTimezone=Asia/Shanghai");
        dsc.setUrl("jdbc:mysql://192.168.1.129:3306/trodb_combine_225?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true&serverTimezone=Asia/Shanghai");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("shulie@2020");
        mbg.setDataSource(dsc);
        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("io.shulie.takin.kafka.receive").setMapper("dao").setXml("dao.mapper");
        mbg.setPackageInfo(pc);
        //策略 设置驼峰命名
        final StrategyConfig sc = new StrategyConfig();
        sc.setInclude("t_scene_manage");
        sc.setTablePrefix("t_");
        sc.setNaming(NamingStrategy.underline_to_camel);
        sc.setColumnNaming(NamingStrategy.underline_to_camel);
        sc.setRestControllerStyle(true);
        sc.setControllerMappingHyphenStyle(false);
        mbg.setStrategy(sc);

        mbg.execute();
    }


}
