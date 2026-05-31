package com.shelfflow.services.user.order.messaging;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.user.config.UserOrderProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Component
public class UserOrderEventPublisher {

    private static final String DEFAULT_ORDER_EVENTS_EXCHANGE = "shelfflow.order.events";
    private static final String DEFAULT_ROUTING_KEY_PREFIX = "shelfflow.order";
    private static final String ROUTING_KEY_SEPARATOR = ".";

    private final RabbitTemplate rabbitTemplate;
    private final UserOrderProperties userOrderProperties;
    private final UserOrderEventOutboxApplicationService outboxApplicationService;

    public UserOrderEventPublisher(RabbitTemplate rabbitTemplate,
                                   UserOrderProperties userOrderProperties,
                                   UserOrderEventOutboxApplicationService outboxApplicationService) {
        this.rabbitTemplate = rabbitTemplate;
        this.userOrderProperties = userOrderProperties;
        this.outboxApplicationService = outboxApplicationService;
    }

    public void publishAfterCommit(UserOrderEventMessage eventMessage) {
        if (!isEnabled() || eventMessage == null) {
            return;
        }

        if (userOrderProperties.getEvents().isOutboxEnabled()) {
            outboxApplicationService.enqueue(
                    eventMessage,
                    resolveMessageId(eventMessage),
                    resolveExchange(),
                    resolveRoutingKey(eventMessage.getEventType()),
                    java.time.LocalDateTime.now()
            );
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(eventMessage);
                }
            });
            return;
        }

        publish(eventMessage);
    }

    public void publishOutboxMessage(UserOrderEventMessage eventMessage,
                                     String messageId,
                                     String exchange,
                                     String routingKey) {
        send(exchange, routingKey, eventMessage, messageId);
    }

    private void publish(UserOrderEventMessage eventMessage) {
        try {
            send(resolveExchange(), resolveRoutingKey(eventMessage.getEventType()), eventMessage, resolveMessageId(eventMessage));
        } catch (AmqpException | IllegalStateException ex) {
            if (userOrderProperties.getEvents().isFailFast()) {
                throw new ApplicationException(ErrorCode.DEPENDENCY_ERROR, "订单事件发布失败，请稍后重试");
            }
            log.error("Order event publish failed. orderId={}, eventType={}",
                    eventMessage.getOrderId(),
                    eventMessage.getEventType(),
                    ex);
        }
    }

    private void send(String exchange, String routingKey, UserOrderEventMessage eventMessage, String messageId) {
        rabbitTemplate.convertAndSend(exchange, routingKey, eventMessage, buildMessagePostProcessor(eventMessage, messageId));
    }

    private MessagePostProcessor buildMessagePostProcessor(UserOrderEventMessage eventMessage, String messageId) {
        return (Message message) -> {
            message.getMessageProperties().setMessageId(messageId);
            message.getMessageProperties().setContentType("application/json");
            message.getMessageProperties().setType(eventMessage.getEventType());
            return message;
        };
    }

    private String resolveMessageId(UserOrderEventMessage eventMessage) {
        if (eventMessage.getEventId() != null) {
            return String.valueOf(eventMessage.getEventId());
        }
        return UUID.randomUUID().toString();
    }

    private boolean isEnabled() {
        UserOrderProperties.Events events = userOrderProperties.getEvents();
        return events != null && events.isEnabled();
    }

    private String resolveExchange() {
        String exchange = userOrderProperties.getEvents().getExchange();
        return StringUtils.hasText(exchange) ? exchange.trim() : DEFAULT_ORDER_EVENTS_EXCHANGE;
    }

    private String resolveRoutingKey(String eventType) {
        String prefix = userOrderProperties.getEvents().getRoutingKeyPrefix();
        String normalizedPrefix = StringUtils.hasText(prefix) ? prefix.trim() : DEFAULT_ROUTING_KEY_PREFIX;
        String normalizedEventType = StringUtils.hasText(eventType) ? eventType.trim() : "unknown";
        if (normalizedPrefix.endsWith(ROUTING_KEY_SEPARATOR)) {
            return normalizedPrefix + normalizedEventType;
        }
        return normalizedPrefix + ROUTING_KEY_SEPARATOR + normalizedEventType;
    }
}
