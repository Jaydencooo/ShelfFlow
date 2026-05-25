package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminLossStatsOverviewResponse {
    private Long totalBatchCount;
    private Long expiredBatchCount;
    private Long expiredStockQuantity;
    private Long expiringSoonBatchCount;
    private Long expiringSoonStockQuantity;
    private Long soldOutBatchCount;
    private Long saleableStockQuantity;
    private BigDecimal estimatedLossAmount;
    private BigDecimal lossRate;
    private BigDecimal revenueAmount;
    private BigDecimal estimatedCostAmount;
    private BigDecimal grossProfitAmount;
    private BigDecimal operatingExpenseAmount;
    private BigDecimal netProfitAmount;
    private BigDecimal lossCostAmount;
    private BigDecimal grossMarginRate;
    private BigDecimal netProfitRate;
    private Long orderCount;
    private Long paidOrderCount;
    private Long soldItemQuantity;
    private BigDecimal averageOrderAmount;
    private List<AdminLossStatsCategoryResponse> categoryStats;
    private List<AdminLossStatsSuggestionResponse> suggestions;
}
