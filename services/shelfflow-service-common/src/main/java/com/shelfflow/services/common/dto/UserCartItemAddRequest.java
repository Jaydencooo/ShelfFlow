package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserCartItemAddRequest {
    @NotBlank
    private String productId;

    private String batchId;

    @Size(max = 50)
    private String productSpec;

    @Min(1)
    private Integer quantity = 1;
}
