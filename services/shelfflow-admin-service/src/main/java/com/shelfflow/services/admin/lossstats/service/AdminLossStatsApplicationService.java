package com.shelfflow.services.admin.lossstats.service;

import com.shelfflow.services.admin.lossstats.config.AdminLossStatsProperties;
import com.shelfflow.services.admin.lossstats.domain.AdminLossStatsPolicy;
import com.shelfflow.services.admin.lossstats.persistence.AdminLossStatsPersistenceMapper;
import com.shelfflow.services.admin.lossstats.persistence.dataobject.AdminLossStatsCategoryRow;
import com.shelfflow.services.admin.lossstats.persistence.dataobject.AdminLossStatsOverviewRow;
import com.shelfflow.services.admin.lossstats.persistence.dataobject.AdminLossStatsSuggestionRow;
import com.shelfflow.services.common.dto.AdminLossStatsCategoryResponse;
import com.shelfflow.services.common.dto.AdminLossStatsOverviewResponse;
import com.shelfflow.services.common.dto.AdminLossStatsSuggestionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminLossStatsApplicationService {

    private final AdminLossStatsPersistenceMapper lossStatsPersistenceMapper;
    private final AdminLossStatsPolicy lossStatsPolicy;
    private final AdminLossStatsProperties properties;

    public AdminLossStatsApplicationService(AdminLossStatsPersistenceMapper lossStatsPersistenceMapper,
                                            AdminLossStatsPolicy lossStatsPolicy,
                                            AdminLossStatsProperties properties) {
        this.lossStatsPersistenceMapper = lossStatsPersistenceMapper;
        this.lossStatsPolicy = lossStatsPolicy;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public AdminLossStatsOverviewResponse overview() {
        AdminLossStatsOverviewRow overview = lossStatsPersistenceMapper.overview(properties.getExpiringSoonDays());
        enrichProfitMetrics(overview);
        List<AdminLossStatsCategoryResponse> categoryStats = lossStatsPersistenceMapper.categoryStats(properties.getExpiringSoonDays()).stream()
                .map(this::toCategoryResponse)
                .toList();
        List<AdminLossStatsSuggestionResponse> suggestions = lossStatsPersistenceMapper
                .suggestions(properties.getExpiringSoonDays(), properties.getSuggestionLimit())
                .stream()
                .map(this::toSuggestionResponse)
                .toList();

        return AdminLossStatsOverviewResponse.builder()
                .totalBatchCount(defaultZero(overview.getTotalBatchCount()))
                .expiredBatchCount(defaultZero(overview.getExpiredBatchCount()))
                .expiredStockQuantity(defaultZero(overview.getExpiredStockQuantity()))
                .expiringSoonBatchCount(defaultZero(overview.getExpiringSoonBatchCount()))
                .expiringSoonStockQuantity(defaultZero(overview.getExpiringSoonStockQuantity()))
                .soldOutBatchCount(defaultZero(overview.getSoldOutBatchCount()))
                .saleableStockQuantity(defaultZero(overview.getSaleableStockQuantity()))
                .estimatedLossAmount(defaultMoney(overview.getEstimatedLossAmount()))
                .lossRate(lossStatsPolicy.calculateLossRate(overview.getExpiredStockQuantity(), overview.getTotalStockQuantity()))
                .revenueAmount(defaultMoney(overview.getRevenueAmount()))
                .estimatedCostAmount(defaultMoney(overview.getEstimatedCostAmount()))
                .grossProfitAmount(defaultMoney(overview.getGrossProfitAmount()))
                .operatingExpenseAmount(defaultMoney(overview.getOperatingExpenseAmount()))
                .netProfitAmount(defaultMoney(overview.getNetProfitAmount()))
                .lossCostAmount(defaultMoney(overview.getLossCostAmount()))
                .grossMarginRate(lossStatsPolicy.calculateAmountRate(overview.getGrossProfitAmount(), overview.getRevenueAmount()))
                .netProfitRate(lossStatsPolicy.calculateAmountRate(overview.getNetProfitAmount(), overview.getRevenueAmount()))
                .orderCount(defaultZero(overview.getOrderCount()))
                .paidOrderCount(defaultZero(overview.getPaidOrderCount()))
                .soldItemQuantity(defaultZero(overview.getSoldItemQuantity()))
                .averageOrderAmount(defaultMoney(overview.getAverageOrderAmount()))
                .categoryStats(categoryStats)
                .suggestions(suggestions)
                .build();
    }

    private void enrichProfitMetrics(AdminLossStatsOverviewRow overview) {
        BigDecimal revenueAmount = defaultMoney(overview.getRevenueAmount());
        BigDecimal estimatedLossAmount = defaultMoney(overview.getEstimatedLossAmount());
        BigDecimal estimatedCostAmount = revenueAmount.multiply(properties.getEstimatedCostRate());
        BigDecimal grossProfitAmount = revenueAmount.subtract(estimatedCostAmount);
        BigDecimal operatingExpenseAmount = defaultMoney(properties.getOperatingExpenseAmount());
        BigDecimal lossCostAmount = estimatedLossAmount.multiply(properties.getEstimatedCostRate());
        BigDecimal netProfitAmount = grossProfitAmount.subtract(operatingExpenseAmount).subtract(lossCostAmount);

        overview.setEstimatedCostAmount(estimatedCostAmount);
        overview.setGrossProfitAmount(grossProfitAmount);
        overview.setOperatingExpenseAmount(operatingExpenseAmount);
        overview.setLossCostAmount(lossCostAmount);
        overview.setNetProfitAmount(netProfitAmount);
    }

    private AdminLossStatsCategoryResponse toCategoryResponse(AdminLossStatsCategoryRow row) {
        return AdminLossStatsCategoryResponse.builder()
                .categoryId(String.valueOf(row.getCategoryId()))
                .categoryName(row.getCategoryName())
                .batchCount(defaultZero(row.getBatchCount()))
                .expiringSoonBatchCount(defaultZero(row.getExpiringSoonBatchCount()))
                .soldOutBatchCount(defaultZero(row.getSoldOutBatchCount()))
                .expiredStockQuantity(defaultZero(row.getExpiredStockQuantity()))
                .estimatedLossAmount(defaultMoney(row.getEstimatedLossAmount()))
                .lossRate(lossStatsPolicy.calculateLossRate(row.getExpiredStockQuantity(), row.getTotalStockQuantity()))
                .build();
    }

    private AdminLossStatsSuggestionResponse toSuggestionResponse(AdminLossStatsSuggestionRow row) {
        return AdminLossStatsSuggestionResponse.builder()
                .id(String.valueOf(row.getBatchId()))
                .batchId(String.valueOf(row.getBatchId()))
                .batchCode(row.getBatchCode())
                .productId(String.valueOf(row.getProductId()))
                .productName(row.getProductName())
                .categoryName(row.getCategoryName())
                .daysToExpire(row.getDaysToExpire())
                .availableStock(row.getAvailableStock())
                .estimatedLossAmount(defaultMoney(row.getEstimatedLossAmount()))
                .priority(lossStatsPolicy.priority(row.getDaysToExpire()))
                .suggestion(lossStatsPolicy.suggestion(row.getDaysToExpire(), row.getEstimatedLossAmount()))
                .action(lossStatsPolicy.action(row.getDaysToExpire()))
                .build();
    }

    private Long defaultZero(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
