package com.shelfflow.services.gateway;

import com.shelfflow.services.common.config.DownstreamServiceProperties;
import com.shelfflow.services.common.config.JwtProperties;
import com.shelfflow.services.common.config.RuntimeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.shelfflow.services.gateway")
@EnableConfigurationProperties({RuntimeProperties.class, JwtProperties.class, DownstreamServiceProperties.class})
public class ShelfFlowGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShelfFlowGatewayApplication.class, args);
    }
}
