package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminLossStatsCategoryResponse {
    private String categoryId;
    private String categoryName;
    private Long batchCount;
    private Long expiringSoonBatchCount;
    private Long soldOutBatchCount;
    private Long expiredStockQuantity;
    private BigDecimal estimatedLossAmount;
    private BigDecimal lossRate;
}
