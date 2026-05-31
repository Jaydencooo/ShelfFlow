package com.shelfflow.services.admin.orderevent.service;

import com.shelfflow.services.admin.orderevent.messaging.AdminOrderEventMessage;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminOrderEventInboxQuery;
import com.shelfflow.services.common.dto.AdminOrderEventInboxResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AdminOrderEventInboxApplicationServiceIntegrationTest {

    @Autowired
    private AdminOrderEventInboxApplicationService inboxApplicationService;

    @Test
    void consumeShouldPersistOrderEventAndIgnoreDuplicateEventId() {
        AdminOrderEventMessage message = buildMessage(910001L, "SFU202605310001");

        AdminOrderEventInboxResponse first = inboxApplicationService.consume(message, "msg-910001", "shelfflow.order.paid");
        AdminOrderEventInboxResponse duplicate = inboxApplicationService.consume(message, "msg-910001-dup", "shelfflow.order.paid");

        assertThat(first.getEventId()).isEqualTo(910001L);
        assertThat(first.getStatus()).isEqualTo("PROCESSED");
        assertThat(first.getProcessedTime()).isNotNull();
        assertThat(duplicate.getId()).isEqualTo(first.getId());

        AdminOrderEventInboxQuery query = new AdminOrderEventInboxQuery();
        query.setOrderNumber("SFU202605310001");
        PageResponse<AdminOrderEventInboxResponse> page = inboxApplicationService.page(query);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getItems()).extracting(AdminOrderEventInboxResponse::getEventType).containsExactly("paid");
    }

    @Test
    void pageShouldFilterByStatusAndEventType() {
        inboxApplicationService.consume(buildMessage(910002L, "SFU202605310002"), "msg-910002", "shelfflow.order.cancelled");

        AdminOrderEventInboxQuery query = new AdminOrderEventInboxQuery();
        query.setEventType("cancelled");
        query.setStatus("PROCESSED");

        PageResponse<AdminOrderEventInboxResponse> page = inboxApplicationService.page(query);

        assertThat(page.getItems()).extracting(AdminOrderEventInboxResponse::getOrderNumber)
                .contains("SFU202605310002");
    }

    private AdminOrderEventMessage buildMessage(Long eventId, String orderNumber) {
        AdminOrderEventMessage message = new AdminOrderEventMessage();
        message.setEventId(eventId);
        message.setOrderId(eventId + 1000);
        message.setOrderNumber(orderNumber);
        message.setUserId(15L);
        message.setEventType(eventId % 2 == 0 ? "cancelled" : "paid");
        message.setActorType("user");
        message.setActorId(15L);
        message.setFromStatus(1);
        message.setToStatus(2);
        message.setFromPayStatus(0);
        message.setToPayStatus(1);
        message.setTotalAmount(new BigDecimal("28.80"));
        message.setItemCount(3);
        message.setNote("integration-test");
        message.setEventTime(LocalDateTime.now());
        return message;
    }
}
