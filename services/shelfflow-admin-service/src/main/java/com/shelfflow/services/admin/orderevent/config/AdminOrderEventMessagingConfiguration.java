package com.shelfflow.services.admin.orderevent.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(prefix = "shelfflow.admin.order-events.consumer", name = "enabled", havingValue = "true")
public class AdminOrderEventMessagingConfiguration {

    private static final String DEFAULT_ORDER_EVENTS_EXCHANGE = "shelfflow.order.events";
    private static final String DEFAULT_ORDER_EVENTS_QUEUE = "shelfflow.admin.order-events.inbox";
    private static final String DEFAULT_ORDER_EVENTS_ROUTING_KEY = "shelfflow.order.*";

    @Bean
    public TopicExchange adminOrderEventsExchange(AdminOrderEventConsumerProperties properties) {
        ExchangeBuilder builder = ExchangeBuilder.topicExchange(resolve(properties.getExchange(), DEFAULT_ORDER_EVENTS_EXCHANGE));
        return properties.isDurableExchange() ? builder.durable(true).build() : builder.durable(false).build();
    }

    @Bean
    public Queue adminOrderEventsQueue(AdminOrderEventConsumerProperties properties) {
        String queueName = resolve(properties.getQueue(), DEFAULT_ORDER_EVENTS_QUEUE);
        return properties.isDurableQueue() ? QueueBuilder.durable(queueName).build() : QueueBuilder.nonDurable(queueName).build();
    }

    @Bean
    public Binding adminOrderEventsBinding(Queue adminOrderEventsQueue,
                                           TopicExchange adminOrderEventsExchange,
                                           AdminOrderEventConsumerProperties properties) {
        return BindingBuilder.bind(adminOrderEventsQueue)
                .to(adminOrderEventsExchange)
                .with(resolve(properties.getRoutingKey(), DEFAULT_ORDER_EVENTS_ROUTING_KEY));
    }

    private String resolve(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
