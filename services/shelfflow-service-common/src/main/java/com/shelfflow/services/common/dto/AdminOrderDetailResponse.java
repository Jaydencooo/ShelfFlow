package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.UserOrderPayStatus;
import com.shelfflow.services.common.domain.UserOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminOrderDetailResponse {
    private String id;
    private String orderNumber;
    private String userId;
    private String userName;
    private String consignee;
    private String phone;
    private UserOrderStatus status;
    private UserOrderPayStatus payStatus;
    private BigDecimal totalAmount;
    private String remark;
    private String pickupPoint;
    private String pickupCode;
    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private LocalDateTime pickupDeadline;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private Integer itemCount;
    private List<AdminOrderItemResponse> items;
    private List<OrderEventResponse> events;
}
