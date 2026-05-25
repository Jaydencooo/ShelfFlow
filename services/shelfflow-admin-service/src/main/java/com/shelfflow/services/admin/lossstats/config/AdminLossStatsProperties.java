package com.shelfflow.services.admin.lossstats.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "shelfflow.admin.loss-stats")
public class AdminLossStatsProperties {

    private int expiringSoonDays = 3;
    private int suggestionLimit = 8;
    private BigDecimal estimatedCostRate = new BigDecimal("0.65");
    private BigDecimal operatingExpenseAmount = BigDecimal.ZERO;

    public int getExpiringSoonDays() {
        return expiringSoonDays;
    }

    public void setExpiringSoonDays(int expiringSoonDays) {
        this.expiringSoonDays = expiringSoonDays;
    }

    public int getSuggestionLimit() {
        return suggestionLimit;
    }

    public void setSuggestionLimit(int suggestionLimit) {
        this.suggestionLimit = suggestionLimit;
    }

    public BigDecimal getEstimatedCostRate() {
        return estimatedCostRate;
    }

    public void setEstimatedCostRate(BigDecimal estimatedCostRate) {
        this.estimatedCostRate = estimatedCostRate == null ? BigDecimal.ZERO : estimatedCostRate;
    }

    public BigDecimal getOperatingExpenseAmount() {
        return operatingExpenseAmount;
    }

    public void setOperatingExpenseAmount(BigDecimal operatingExpenseAmount) {
        this.operatingExpenseAmount = operatingExpenseAmount == null ? BigDecimal.ZERO : operatingExpenseAmount;
    }
}
