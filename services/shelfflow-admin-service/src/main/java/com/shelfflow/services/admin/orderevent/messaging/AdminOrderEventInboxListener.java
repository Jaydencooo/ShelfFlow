package com.shelfflow.services.admin.orderevent.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelfflow.services.admin.orderevent.config.AdminOrderEventConsumerProperties;
import com.shelfflow.services.admin.orderevent.service.AdminOrderEventInboxApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "shelfflow.admin.order-events.consumer", name = "enabled", havingValue = "true")
public class AdminOrderEventInboxListener {

    private final ObjectMapper objectMapper;
    private final AdminOrderEventInboxApplicationService inboxApplicationService;
    private final AdminOrderEventConsumerProperties properties;

    public AdminOrderEventInboxListener(ObjectMapper objectMapper,
                                        AdminOrderEventInboxApplicationService inboxApplicationService,
                                        AdminOrderEventConsumerProperties properties) {
        this.objectMapper = objectMapper;
        this.inboxApplicationService = inboxApplicationService;
        this.properties = properties;
    }

    @RabbitListener(queues = "#{adminOrderEventsQueue.name}")
    public void onMessage(Message message) {
        AdminOrderEventMessage eventMessage = null;
        try {
            eventMessage = objectMapper.readValue(message.getBody(), AdminOrderEventMessage.class);
            inboxApplicationService.consume(
                    eventMessage,
                    message.getMessageProperties().getMessageId(),
                    message.getMessageProperties().getReceivedRoutingKey()
            );
        } catch (Exception ex) {
            Long eventId = eventMessage == null ? null : eventMessage.getEventId();
            inboxApplicationService.markFailed(eventId, ex.getMessage());
            log.error("Admin order event consume failed. eventId={}", eventId, ex);
            if (properties.isFailFast()) {
                throw new IllegalStateException("订单事件消费失败", ex);
            }
        }
    }
}
