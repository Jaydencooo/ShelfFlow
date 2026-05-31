package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminOrderEventInboxResponse {
    private String id;
    private Long eventId;
    private String messageId;
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
    private String routingKey;
    private String status;
    private Integer retryCount;
    private String failureReason;
    private LocalDateTime eventTime;
    private LocalDateTime receivedTime;
    private LocalDateTime processedTime;
}
