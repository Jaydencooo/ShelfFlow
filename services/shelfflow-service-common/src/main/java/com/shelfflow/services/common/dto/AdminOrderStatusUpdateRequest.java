package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.UserOrderStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AdminOrderStatusUpdateRequest {
    @NotNull
    private UserOrderStatus orderStatus;
}
