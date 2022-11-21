package io.shulie.surge.data.deploy.pradar;

import io.shulie.surge.data.deploy.pradar.config.PradarLinkConfiguration;
import io.shulie.surge.data.deploy.pradar.config.PradarModule;
import io.shulie.surge.data.runtime.common.DataBootstrap;
import io.shulie.surge.data.runtime.common.remote.impl.RemoteNacosModule;
import io.shulie.surge.data.runtime.module.NacosClientModule;
import io.shulie.surge.data.sink.clickhouse.ClickHouseModule;
import io.shulie.surge.data.sink.mysql.MysqlModule;

/**
 * @author vincent
 * @date 2022/11/14 17:46
 **/
public class PradarKafkaLinkConfiguration extends PradarLinkConfiguration {

    /**
     * 装载module
     *
     * @param bootstrap
     */
    @Override
    public void install(DataBootstrap bootstrap) {
        bootstrap.install(new PradarModule(), new ClickHouseModule(), new MysqlModule(), new NacosClientModule(), new RemoteNacosModule());
    }
}
