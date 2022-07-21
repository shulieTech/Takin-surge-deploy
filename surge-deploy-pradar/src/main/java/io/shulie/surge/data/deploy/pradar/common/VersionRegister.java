package io.shulie.surge.data.deploy.pradar.common;

import java.io.InputStream;
import java.util.Properties;

import javax.inject.Singleton;

import com.alibaba.fastjson.JSON;

import com.google.inject.Inject;
import io.shulie.surge.data.common.zk.ZkClient;
import io.shulie.surge.data.common.zk.ZkClient.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

@Singleton
public class VersionRegister {

    private static final Logger log = LoggerFactory.getLogger(VersionRegister.class);

    @Inject
    private ZkClient zkClient;

    private static final String REGISTER_PATH = "/pradar/config/version/surge";

    public void init() {
        try {
            zkClient.deleteQuietly(REGISTER_PATH);
            zkClient.ensureParentExists(REGISTER_PATH);
            zkClient.createNode(REGISTER_PATH, JSON.toJSONBytes(readGitVersion()), CreateMode.PERSISTENT);
        } catch (Exception e) {
            log.error("注册版本信息异常", e);
        }
    }

    public static Properties readGitVersion() {
        Properties gitVersion = new Properties();
        Resource resource = new DefaultResourceLoader().getResource("classpath:git.properties");
        if (resource.exists()) {
            try (InputStream stream = resource.getInputStream()) {
                gitVersion.load(stream);
            } catch (Exception ignore) {
            }
        }
        return gitVersion;
    }
}
