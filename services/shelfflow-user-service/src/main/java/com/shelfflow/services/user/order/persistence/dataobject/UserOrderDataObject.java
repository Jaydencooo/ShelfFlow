package com.shelfflow.services.user.order.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserOrderDataObject {
    private Long id;
    private String number;
    private Integer status;
    private Long userId;
    private Long pickupContactId;
    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private Integer payMethod;
    private Integer payStatus;
    private BigDecimal amount;
    private String remark;
    private String phone;
    private String pickupPoint;
    private String userName;
    private String consignee;
    private Integer preparationMode;
    private Integer fulfillmentFee;
    private Integer packageCount;
    private Integer packageStrategy;
    private Integer fulfillmentType;
    private String pickupCode;
    private LocalDateTime pickupDeadline;
}
