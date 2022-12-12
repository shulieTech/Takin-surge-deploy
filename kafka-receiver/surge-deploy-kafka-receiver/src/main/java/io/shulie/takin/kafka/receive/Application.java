package io.shulie.takin.kafka.receive;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = {"io.shulie.takin.kafka.receive"})
public class Application {

    public static void main(String[] args) throws IOException {
        SpringApplicationBuilder applicationBuilder = new SpringApplicationBuilder().sources(Application.class);
        applicationBuilder.run(args);
    }

}
