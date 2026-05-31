package com.shelfflow.services.user.order.messaging;

import com.shelfflow.services.user.order.persistence.dataobject.UserOrderEventOutboxDataObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserOrderEventOutboxApplicationServiceIntegrationTest {

    @Autowired
    private UserOrderEventOutboxApplicationService outboxApplicationService;

    @Test
    void enqueueShouldPersistPendingEventAndKeepEventIdIdempotent() {
        LocalDateTime now = LocalDateTime.now();
        UserOrderEventMessage message = buildMessage(920001L);

        outboxApplicationService.enqueue(message, "920001", "shelfflow.order.events", "shelfflow.order.paid", now);
        outboxApplicationService.enqueue(message, "920001-duplicate", "shelfflow.order.events", "shelfflow.order.paid", now);

        List<UserOrderEventOutboxDataObject> pending = outboxApplicationService.listPendingDue(now.plusSeconds(1), 10, 5);

        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getEventId()).isEqualTo(920001L);
        assertThat(pending.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(outboxApplicationService.deserialize(pending.get(0)).getOrderNumber()).isEqualTo("SFU202605310001");
    }

    @Test
    void markPublishedAndFailedShouldUpdateDeliveryState() {
        LocalDateTime now = LocalDateTime.now();
        outboxApplicationService.enqueue(buildMessage(920002L), "920002", "shelfflow.order.events", "shelfflow.order.submitted", now);
        UserOrderEventOutboxDataObject pending = outboxApplicationService.listPendingDue(now.plusSeconds(1), 10, 5).get(0);

        outboxApplicationService.markFailed(pending.getId(), now.plusSeconds(60), "temporary failure", now.plusSeconds(1));
        List<UserOrderEventOutboxDataObject> retryDue = outboxApplicationService.listPendingDue(now.plusSeconds(61), 10, 5);

        assertThat(retryDue).extracting(UserOrderEventOutboxDataObject::getStatus).contains("FAILED");

        outboxApplicationService.markPublished(pending.getId(), now.plusSeconds(62));
        List<UserOrderEventOutboxDataObject> remaining = outboxApplicationService.listPendingDue(now.plusSeconds(120), 10, 5);

        assertThat(remaining).isEmpty();
    }

    private UserOrderEventMessage buildMessage(Long eventId) {
        return UserOrderEventMessage.builder()
                .eventId(eventId)
                .orderId(eventId + 1000)
                .orderNumber("SFU202605310001")
                .userId(15L)
                .eventType("paid")
                .actorType("user")
                .actorId(15L)
                .fromStatus(1)
                .toStatus(2)
                .fromPayStatus(0)
                .toPayStatus(1)
                .totalAmount(new BigDecimal("19.90"))
                .itemCount(2)
                .note("outbox-test")
                .eventTime(LocalDateTime.now())
                .build();
    }
}
