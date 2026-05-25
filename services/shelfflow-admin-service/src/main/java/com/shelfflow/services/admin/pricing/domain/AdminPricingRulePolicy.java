package com.shelfflow.services.admin.pricing.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AdminPricingRulePolicy {

    private static final BigDecimal HIGH_CONFIDENCE_DISCOUNT = new BigDecimal("0.50");
    private static final BigDecimal MEDIUM_CONFIDENCE_DISCOUNT = new BigDecimal("0.70");
    private static final BigDecimal LOW_CONFIDENCE_DISCOUNT = new BigDecimal("0.85");
    private static final int HIGH_CONFIDENCE_MAX_DAYS = 2;
    private static final int MEDIUM_CONFIDENCE_MAX_DAYS = 7;

    public String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "规则名称不能为空");
        }
        return normalized;
    }

    public void ensureValidDayRange(Integer minDaysToExpire, Integer maxDaysToExpire) {
        if (minDaysToExpire == null || maxDaysToExpire == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "过期天数范围不能为空");
        }
        if (minDaysToExpire > maxDaysToExpire) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "最小过期天数不能大于最大过期天数");
        }
    }

    public void ensureUniqueName(Long existingId, Long currentRuleId) {
        if (existingId == null) {
            return;
        }
        if (currentRuleId == null || !existingId.equals(currentRuleId)) {
            throw new ApplicationException(ErrorCode.CONFLICT, "定价规则名称已存在");
        }
    }

    public BigDecimal suggestDiscountRate(Integer daysToExpire) {
        if (daysToExpire == null || daysToExpire <= HIGH_CONFIDENCE_MAX_DAYS) {
            return HIGH_CONFIDENCE_DISCOUNT;
        }
        if (daysToExpire <= MEDIUM_CONFIDENCE_MAX_DAYS) {
            return MEDIUM_CONFIDENCE_DISCOUNT;
        }
        return LOW_CONFIDENCE_DISCOUNT;
    }

    public String confidence(Integer daysToExpire) {
        if (daysToExpire == null || daysToExpire <= HIGH_CONFIDENCE_MAX_DAYS) {
            return "高";
        }
        if (daysToExpire <= MEDIUM_CONFIDENCE_MAX_DAYS) {
            return "中";
        }
        return "低";
    }

    public BigDecimal calculateSuggestedPrice(BigDecimal currentPrice, BigDecimal discountRate) {
        if (currentPrice == null || discountRate == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return currentPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }
}
