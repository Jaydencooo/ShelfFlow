package com.shelfflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 履约任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentTask implements Serializable {

    public static final Integer WAITING_PREPARE = 1;
    public static final Integer READY_FOR_PICKUP = 2;
    public static final Integer COMPLETED = 3;
    public static final Integer CANCELLED = 4;

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long orderId;

    private String orderNumber;

    private String pickupCode;

    private Integer status;

    private LocalDateTime pickupDeadline;

    private LocalDateTime completedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
