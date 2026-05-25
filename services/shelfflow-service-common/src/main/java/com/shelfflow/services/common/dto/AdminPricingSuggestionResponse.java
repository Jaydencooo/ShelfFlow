package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminPricingSuggestionResponse {
    private String id;
    private String batchId;
    private String batchCode;
    private String productId;
    private String productName;
    private Integer daysToExpire;
    private Integer availableStock;
    private BigDecimal currentPrice;
    private BigDecimal suggestedPrice;
    private BigDecimal suggestedDiscountRate;
    private String confidence;
    private String reason;
}
