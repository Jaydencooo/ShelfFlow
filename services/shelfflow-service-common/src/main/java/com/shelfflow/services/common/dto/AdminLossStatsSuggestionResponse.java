package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminLossStatsSuggestionResponse {
    private String id;
    private String batchId;
    private String batchCode;
    private String productId;
    private String productName;
    private String categoryName;
    private Integer daysToExpire;
    private Integer availableStock;
    private BigDecimal estimatedLossAmount;
    private String priority;
    private String suggestion;
    private String action;
}
