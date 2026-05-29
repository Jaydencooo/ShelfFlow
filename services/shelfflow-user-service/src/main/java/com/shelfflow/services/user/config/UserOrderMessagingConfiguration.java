package com.shelfflow.services.user.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(prefix = "shelfflow.user.order.events", name = "enabled", havingValue = "true")
public class UserOrderMessagingConfiguration {

    private static final String DEFAULT_ORDER_EVENTS_EXCHANGE = "shelfflow.order.events";

    @Bean
    public TopicExchange userOrderEventsExchange(UserOrderProperties userOrderProperties) {
        UserOrderProperties.Events events = userOrderProperties.getEvents();
        String exchangeName = StringUtils.hasText(events.getExchange())
                ? events.getExchange().trim()
                : DEFAULT_ORDER_EVENTS_EXCHANGE;
        ExchangeBuilder builder = ExchangeBuilder.topicExchange(exchangeName);
        if (events.isDurableExchange()) {
            builder.durable(true);
        } else {
            builder.durable(false);
        }
        return builder.build();
    }
}
