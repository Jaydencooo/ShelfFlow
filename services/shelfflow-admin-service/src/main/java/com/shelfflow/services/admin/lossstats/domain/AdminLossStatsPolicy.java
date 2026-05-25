package com.shelfflow.services.admin.lossstats.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AdminLossStatsPolicy {

    private static final int RATE_SCALE = 4;
    private static final int EXPIRED_DAYS_THRESHOLD = 0;
    private static final int HIGH_PRIORITY_DAYS_THRESHOLD = 1;
    private static final int MEDIUM_PRIORITY_DAYS_THRESHOLD = 3;
    private static final String HIGH_PRIORITY = "高";
    private static final String MEDIUM_PRIORITY = "中";
    private static final String LOW_PRIORITY = "低";

    public BigDecimal calculateLossRate(Long expiredStockQuantity, Long totalStockQuantity) {
        long totalStock = defaultZero(totalStockQuantity);
        if (totalStock <= 0) {
            return BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(defaultZero(expiredStockQuantity))
                .divide(BigDecimal.valueOf(totalStock), RATE_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateAmountRate(BigDecimal numerator, BigDecimal denominator) {
        BigDecimal safeDenominator = denominator == null ? BigDecimal.ZERO : denominator;
        if (safeDenominator.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal safeNumerator = numerator == null ? BigDecimal.ZERO : numerator;
        return safeNumerator.divide(safeDenominator, RATE_SCALE, RoundingMode.HALF_UP);
    }

    public String priority(Integer daysToExpire) {
        int days = daysToExpire == null ? MEDIUM_PRIORITY_DAYS_THRESHOLD : daysToExpire;
        if (days <= HIGH_PRIORITY_DAYS_THRESHOLD) {
            return HIGH_PRIORITY;
        }
        if (days <= MEDIUM_PRIORITY_DAYS_THRESHOLD) {
            return MEDIUM_PRIORITY;
        }
        return LOW_PRIORITY;
    }

    public String suggestion(Integer daysToExpire, BigDecimal estimatedLossAmount) {
        int days = daysToExpire == null ? MEDIUM_PRIORITY_DAYS_THRESHOLD : daysToExpire;
        String amount = estimatedLossAmount == null ? "0.00" : estimatedLossAmount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        if (days <= EXPIRED_DAYS_THRESHOLD) {
            return "批次已过期或当天到期，建议立即下架盘点，预估可避免损耗 ¥" + amount;
        }
        if (days <= HIGH_PRIORITY_DAYS_THRESHOLD) {
            return "剩余 " + days + " 天过期，建议立即加大折扣并同步前置仓处理";
        }
        if (days <= MEDIUM_PRIORITY_DAYS_THRESHOLD) {
            return "剩余 " + days + " 天过期，建议加入临期专区并提高曝光";
        }
        return "剩余 " + days + " 天过期，建议持续观察动销并准备折扣策略";
    }

    public String action(Integer daysToExpire) {
        int days = daysToExpire == null ? MEDIUM_PRIORITY_DAYS_THRESHOLD : daysToExpire;
        if (days <= EXPIRED_DAYS_THRESHOLD) {
            return "下架盘点";
        }
        if (days <= HIGH_PRIORITY_DAYS_THRESHOLD) {
            return "立即处理";
        }
        if (days <= MEDIUM_PRIORITY_DAYS_THRESHOLD) {
            return "设置折扣";
        }
        return "持续观察";
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }
}
