package com.shelfflow.services.user.order.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserOrderEventDataObject {
    private Long id;
    private Long orderId;
    private String eventType;
    private String actorType;
    private Long actorId;
    private Integer fromStatus;
    private Integer toStatus;
    private Integer fromPayStatus;
    private Integer toPayStatus;
    private String note;
    private LocalDateTime createTime;
}
