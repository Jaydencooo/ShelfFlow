package com.shelfflow.services.user;

import com.shelfflow.services.common.config.JwtProperties;
import com.shelfflow.services.common.config.RuntimeProperties;
import com.shelfflow.services.user.config.UserAuthProperties;
import com.shelfflow.services.user.config.UserOrderProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.shelfflow.services")
@MapperScan({
        "com.shelfflow.services.user.auth.persistence",
        "com.shelfflow.services.user.cart.persistence",
        "com.shelfflow.services.user.catalog.persistence",
        "com.shelfflow.services.user.order.persistence",
        "com.shelfflow.services.user.pickupcontact.persistence",
        "com.shelfflow.services.user.pickuppoint.persistence"
})
@EnableConfigurationProperties({RuntimeProperties.class, JwtProperties.class, UserOrderProperties.class, UserAuthProperties.class})
public class ShelfFlowUserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShelfFlowUserServiceApplication.class, args);
    }
}
