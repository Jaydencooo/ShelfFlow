package com.shelfflow.services.admin;

import com.shelfflow.services.admin.aiops.config.AdminAiOpsProperties;
import com.shelfflow.services.admin.lossstats.config.AdminLossStatsProperties;
import com.shelfflow.services.common.config.JwtProperties;
import com.shelfflow.services.common.config.LegacyProperties;
import com.shelfflow.services.common.config.RuntimeProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.shelfflow.services")
@EnableFeignClients
@MapperScan("com.shelfflow.services.admin")
@EnableConfigurationProperties({RuntimeProperties.class, JwtProperties.class, LegacyProperties.class, AdminLossStatsProperties.class, AdminAiOpsProperties.class})
public class ShelfFlowAdminServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShelfFlowAdminServiceApplication.class, args);
    }
}
