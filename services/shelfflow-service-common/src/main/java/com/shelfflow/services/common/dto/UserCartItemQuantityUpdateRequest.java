package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class UserCartItemQuantityUpdateRequest {

    @Min(1)
    private Integer quantity = 1;
}
