package com.shelfflow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentTaskVO implements Serializable {

    private Long id;

    private Long orderId;

    private String orderNumber;

    private String pickupCode;

    private Integer status;

    private Integer orderStatus;

    private BigDecimal amount;

    private String phone;

    private String consignee;

    private String remark;

    private LocalDateTime pickupDeadline;

    private LocalDateTime completedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
