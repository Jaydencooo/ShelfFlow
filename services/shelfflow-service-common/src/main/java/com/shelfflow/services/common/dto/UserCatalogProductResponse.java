package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserCatalogProductResponse {
    private String id;
    private String name;
    private String categoryId;
    private String categoryName;
    private String image;
    private String description;
    private BigDecimal listPrice;
    private BigDecimal currentPrice;
    private String recommendedBatchId;
    private String nearestExpiryDate;
    private Integer daysToExpire;
    private Integer availableQuantity;
}
