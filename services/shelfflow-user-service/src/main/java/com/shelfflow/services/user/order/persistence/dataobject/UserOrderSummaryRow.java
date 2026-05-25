package com.shelfflow.services.user.order.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserOrderSummaryRow {
    private Long id;
    private String number;
    private Integer status;
    private Integer payStatus;
    private BigDecimal amount;
    private String remark;
    private String pickupCode;
    private LocalDateTime orderTime;
    private LocalDateTime pickupDeadline;
}
