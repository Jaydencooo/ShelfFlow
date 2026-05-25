package com.shelfflow.services.admin.order.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderSummaryRow {
    private Long id;
    private String number;
    private Integer status;
    private Integer payStatus;
    private Long userId;
    private String userName;
    private String consignee;
    private String phone;
    private BigDecimal amount;
    private String pickupCode;
    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private LocalDateTime pickupDeadline;
    private Integer itemCount;
}
