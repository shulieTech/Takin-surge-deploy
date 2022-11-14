package io.shulie.surge.data.deploy.pradar.spring;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @description:
 * @date 2022/7/15 14:39
 */
public class SpringBeanFactory {

    private static ClassPathXmlApplicationContext SPRING_CONTEXT;
    public static final String ACTIVE_KEY_NAME = "spring.profiles.active";

    public static synchronized ClassPathXmlApplicationContext getInstanceWithProfile(String profile) {
        ClassPathXmlApplicationContext classPathXmlApplicationContext =
                new ClassPathXmlApplicationContext(new String[]{"classpath*:application.xml"}, false);
        if (StringUtils.isNotBlank(profile)) {
            classPathXmlApplicationContext.getEnvironment().setActiveProfiles(profile);
        }
        classPathXmlApplicationContext.refresh();
        return classPathXmlApplicationContext;
    }

}
