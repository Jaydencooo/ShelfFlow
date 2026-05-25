package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserCartItemResponse {
    private String id;
    private String productId;
    private String batchId;
    private String name;
    private String image;
    private String productSpec;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private Integer availableQuantity;
    private String nearestExpiryDate;
}
