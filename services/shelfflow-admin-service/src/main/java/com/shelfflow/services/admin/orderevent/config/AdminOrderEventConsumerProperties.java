package com.shelfflow.services.admin.orderevent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shelfflow.admin.order-events.consumer")
public class AdminOrderEventConsumerProperties {
    private boolean enabled;
    private String exchange = "shelfflow.order.events";
    private String queue = "shelfflow.admin.order-events.inbox";
    private String routingKey = "shelfflow.order.*";
    private boolean durableExchange = true;
    private boolean durableQueue = true;
    private boolean failFast = true;
}
