package com.shelfflow.services.user.order.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserOrderPaymentDataObject {
    private Long id;
    private String paymentNo;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private BigDecimal amount;
    private Integer payMethod;
    private String provider;
    private Integer status;
    private String idempotencyKey;
    private String externalTradeNo;
    private String callbackEventId;
    private LocalDateTime requestTime;
    private LocalDateTime paidTime;
    private LocalDateTime callbackTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
