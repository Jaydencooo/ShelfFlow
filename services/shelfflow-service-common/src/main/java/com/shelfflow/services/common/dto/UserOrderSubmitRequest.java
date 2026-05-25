package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UserOrderSubmitRequest {
    @Size(max = 100)
    private String remark;

    private String pickupContactId;
}
