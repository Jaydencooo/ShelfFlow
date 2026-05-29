package com.shelfflow.services.user.order.messaging;

import com.shelfflow.services.user.order.persistence.dataobject.UserOrderEventDataObject;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class UserOrderEventMessage {
    Long eventId;
    Long orderId;
    String orderNumber;
    Long userId;
    String eventType;
    String actorType;
    Long actorId;
    Integer fromStatus;
    Integer toStatus;
    Integer fromPayStatus;
    Integer toPayStatus;
    BigDecimal totalAmount;
    Integer itemCount;
    String note;
    LocalDateTime eventTime;

    public static UserOrderEventMessage from(UserOrderEventDataObject event,
                                             String orderNumber,
                                             Long userId,
                                             BigDecimal totalAmount,
                                             Integer itemCount) {
        return UserOrderEventMessage.builder()
                .eventId(event.getId())
                .orderId(event.getOrderId())
                .orderNumber(orderNumber)
                .userId(userId)
                .eventType(event.getEventType())
                .actorType(event.getActorType())
                .actorId(event.getActorId())
                .fromStatus(event.getFromStatus())
                .toStatus(event.getToStatus())
                .fromPayStatus(event.getFromPayStatus())
                .toPayStatus(event.getToPayStatus())
                .totalAmount(totalAmount)
                .itemCount(itemCount)
                .note(event.getNote())
                .eventTime(event.getCreateTime())
                .build();
    }
}
