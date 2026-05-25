package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.UserOrderPayStatus;
import com.shelfflow.services.common.domain.UserOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminOrderSummaryResponse {
    private String id;
    private String orderNumber;
    private String userId;
    private String userName;
    private String consignee;
    private String phone;
    private UserOrderStatus status;
    private UserOrderPayStatus payStatus;
    private BigDecimal totalAmount;
    private String pickupCode;
    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private LocalDateTime pickupDeadline;
    private Integer itemCount;
}
