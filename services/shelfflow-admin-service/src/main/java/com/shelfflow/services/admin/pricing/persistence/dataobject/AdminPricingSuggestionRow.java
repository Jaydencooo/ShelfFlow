package com.shelfflow.services.admin.pricing.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminPricingSuggestionRow {
    private Long batchId;
    private String batchCode;
    private Long productId;
    private String productName;
    private Integer daysToExpire;
    private Integer availableStock;
    private BigDecimal currentPrice;
}
