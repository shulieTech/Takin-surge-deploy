package io.shulie.takin.kafka.receiver;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"io.shulie.takin.kafka.receiver"})
public class Application {

    public static void main(String[] args) throws IOException {
        SpringApplicationBuilder applicationBuilder = new SpringApplicationBuilder().sources(Application.class);
        applicationBuilder.run(args);
    }

}
