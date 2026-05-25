package com.shelfflow.services.user.catalog.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class UserCatalogPolicy {

    public static final int PRODUCT_CATEGORY_TYPE = 1;
    public static final int ACTIVE_STATUS = 1;
    private static final BigDecimal DEFAULT_DISCOUNT_RATE = BigDecimal.ONE;

    public String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long parseOptionalCategoryId(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(categoryId.trim());
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "categoryId 必须为数字");
        }
    }

    public Long parseRequiredProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "productId 不能为空");
        }
        try {
            return Long.valueOf(productId.trim());
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "productId 必须为数字");
        }
    }

    public String resolveSortColumn(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "p.update_time";
        }
        return switch (sortBy) {
            case "price" -> "p.price";
            case "daysToExpire" -> "nearest_expiration_time";
            case "updatedAt" -> "p.update_time";
            default -> throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "sortBy 不支持");
        };
    }

    public BigDecimal calculateCurrentPrice(BigDecimal listPrice, Integer daysToExpire, BigDecimal discountRate) {
        if (listPrice == null) {
            throw new ApplicationException(ErrorCode.INTERNAL_ERROR, "商品价格缺失");
        }
        if (daysToExpire == null || daysToExpire < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal effectiveRate = discountRate == null ? DEFAULT_DISCOUNT_RATE : discountRate;
        return listPrice.multiply(effectiveRate).setScale(2, RoundingMode.HALF_UP);
    }

    public String formatExpiryDate(LocalDateTime nearestExpirationTime) {
        return nearestExpirationTime == null ? null : nearestExpirationTime.toLocalDate().toString();
    }

    public Integer calculateDaysToExpire(LocalDateTime nearestExpirationTime) {
        if (nearestExpirationTime == null) {
            return null;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nearestExpirationTime.toLocalDate());
        return (int) days;
    }

    public void ensureSellableProductExists(boolean exists) {
        if (!exists) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "商品不存在或当前不可售");
        }
    }
}
