package com.shelfflow.services.admin.lossstats.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminLossStatsCategoryRow {
    private Long categoryId;
    private String categoryName;
    private Long batchCount;
    private Long expiringSoonBatchCount;
    private Long soldOutBatchCount;
    private Long expiredStockQuantity;
    private Long totalStockQuantity;
    private BigDecimal estimatedLossAmount;
}
