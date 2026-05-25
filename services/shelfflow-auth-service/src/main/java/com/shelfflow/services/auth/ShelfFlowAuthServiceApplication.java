package com.shelfflow.services.auth;

import com.shelfflow.services.common.config.JwtProperties;
import com.shelfflow.services.common.config.LegacyProperties;
import com.shelfflow.services.common.config.RuntimeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.shelfflow.services")
@EnableFeignClients
@EnableConfigurationProperties({RuntimeProperties.class, JwtProperties.class, LegacyProperties.class})
public class ShelfFlowAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShelfFlowAuthServiceApplication.class, args);
    }
}
