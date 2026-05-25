package com.shelfflow.services.admin.order.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOrderEventRow {
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
