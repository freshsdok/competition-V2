package com.teaching.implatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.teaching.common.security.annotation.EnableCustomConfig;
import com.teaching.common.security.annotation.EnableRyFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCustomConfig
@EnableRyFeignClients
@ComponentScan(basePackages = {
        "com.teaching.implatform",
        "com.teaching.imcommon"
})
public class IMPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(IMPlatformApplication.class, args);
    }

}
