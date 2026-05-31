package com.shelfflow.services.user.order.messaging;

import com.shelfflow.services.user.config.UserOrderProperties;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderEventOutboxDataObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "shelfflow.user.order.events", name = "outbox-enabled", havingValue = "true")
public class UserOrderEventOutboxScheduler {

    private final UserOrderEventOutboxApplicationService outboxApplicationService;
    private final UserOrderEventPublisher eventPublisher;
    private final UserOrderProperties userOrderProperties;

    public UserOrderEventOutboxScheduler(UserOrderEventOutboxApplicationService outboxApplicationService,
                                         UserOrderEventPublisher eventPublisher,
                                         UserOrderProperties userOrderProperties) {
        this.outboxApplicationService = outboxApplicationService;
        this.eventPublisher = eventPublisher;
        this.userOrderProperties = userOrderProperties;
    }

    @Scheduled(
            initialDelayString = "${shelfflow.user.order.events.outbox-initial-delay-milliseconds:60000}",
            fixedDelayString = "${shelfflow.user.order.events.outbox-fixed-delay-milliseconds:60000}"
    )
    public void publishPendingEvents() {
        UserOrderProperties.Events events = userOrderProperties.getEvents();
        if (!events.isEnabled()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<UserOrderEventOutboxDataObject> pendingEvents = outboxApplicationService.listPendingDue(
                now,
                events.getOutboxBatchSize(),
                events.getOutboxMaxAttempts()
        );
        for (UserOrderEventOutboxDataObject outbox : pendingEvents) {
            publishOne(outbox, now);
        }
    }

    private void publishOne(UserOrderEventOutboxDataObject outbox, LocalDateTime now) {
        try {
            UserOrderEventMessage eventMessage = outboxApplicationService.deserialize(outbox);
            eventPublisher.publishOutboxMessage(eventMessage, outbox.getMessageId(), outbox.getExchangeName(), outbox.getRoutingKey());
            outboxApplicationService.markPublished(outbox.getId(), LocalDateTime.now());
        } catch (Exception ex) {
            LocalDateTime nextRetryTime = now.plusSeconds(userOrderProperties.getEvents().getOutboxRetryDelaySeconds());
            outboxApplicationService.markFailed(outbox.getId(), nextRetryTime, ex.getMessage(), LocalDateTime.now());
            log.error("User order event outbox publish failed. outboxId={}, eventId={}", outbox.getId(), outbox.getEventId(), ex);
        }
    }
}
