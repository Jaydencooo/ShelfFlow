package com.shelfflow.services.admin.orderevent.messaging;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderEventMessage {
    private Long eventId;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String eventType;
    private String actorType;
    private Long actorId;
    private Integer fromStatus;
    private Integer toStatus;
    private Integer fromPayStatus;
    private Integer toPayStatus;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private String note;
    private LocalDateTime eventTime;
}
