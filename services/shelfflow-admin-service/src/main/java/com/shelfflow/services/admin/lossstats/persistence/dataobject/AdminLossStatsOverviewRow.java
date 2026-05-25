package com.shelfflow.services.admin.lossstats.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminLossStatsOverviewRow {
    private Long totalBatchCount;
    private Long totalStockQuantity;
    private Long expiredBatchCount;
    private Long expiredStockQuantity;
    private Long expiringSoonBatchCount;
    private Long expiringSoonStockQuantity;
    private Long soldOutBatchCount;
    private Long saleableStockQuantity;
    private BigDecimal estimatedLossAmount;
    private BigDecimal revenueAmount;
    private BigDecimal estimatedCostAmount;
    private BigDecimal grossProfitAmount;
    private BigDecimal operatingExpenseAmount;
    private BigDecimal netProfitAmount;
    private BigDecimal lossCostAmount;
    private Long orderCount;
    private Long paidOrderCount;
    private Long soldItemQuantity;
    private BigDecimal averageOrderAmount;
}
