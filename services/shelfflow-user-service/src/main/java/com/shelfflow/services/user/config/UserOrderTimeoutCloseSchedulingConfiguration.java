package com.shelfflow.services.user.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "shelfflow.user.order.timeout-close", name = "enabled", havingValue = "true")
public class UserOrderTimeoutCloseSchedulingConfiguration {
}
