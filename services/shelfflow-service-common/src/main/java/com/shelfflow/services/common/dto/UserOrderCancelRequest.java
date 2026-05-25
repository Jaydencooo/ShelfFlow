package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UserOrderCancelRequest {
    @Size(max = 100)
    private String cancelReason;
}
