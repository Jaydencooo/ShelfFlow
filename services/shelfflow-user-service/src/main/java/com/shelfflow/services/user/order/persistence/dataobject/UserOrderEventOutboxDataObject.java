package com.shelfflow.services.user.order.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserOrderEventOutboxDataObject {
    private Long id;
    private Long eventId;
    private String messageId;
    private String exchangeName;
    private String routingKey;
    private String payloadJson;
    private String status;
    private Integer attemptCount;
    private LocalDateTime nextRetryTime;
    private String lastError;
    private LocalDateTime publishedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
