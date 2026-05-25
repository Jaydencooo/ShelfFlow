package com.shelfflow.services.admin.lossstats.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminLossStatsSuggestionRow {
    private Long batchId;
    private String batchCode;
    private Long productId;
    private String productName;
    private String categoryName;
    private Integer daysToExpire;
    private Integer availableStock;
    private BigDecimal estimatedLossAmount;
}
