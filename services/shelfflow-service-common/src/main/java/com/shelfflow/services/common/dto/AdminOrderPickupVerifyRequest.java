package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AdminOrderPickupVerifyRequest {

    @NotBlank(message = "自提码不能为空")
    @Size(max = 32, message = "自提码不能超过 32 位")
    private String pickupCode;
}
