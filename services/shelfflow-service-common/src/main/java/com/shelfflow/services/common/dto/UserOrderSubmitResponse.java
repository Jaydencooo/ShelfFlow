package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.UserOrderPayStatus;
import com.shelfflow.services.common.domain.UserOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserOrderSubmitResponse {
    private String id;
    private String orderNumber;
    private UserOrderStatus status;
    private UserOrderPayStatus payStatus;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private String pickupCode;
    private LocalDateTime orderTime;
    private LocalDateTime pickupDeadline;
}
