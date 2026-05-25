package com.shelfflow.services.admin.inventorybatch.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class BatchInventoryPolicy {

    public static final int INITIAL_LOCKED_STOCK = 0;
    public static final int INITIAL_SOLD_STOCK = 0;

    public void validateDateRange(LocalDateTime productionTime, LocalDateTime expirationTime) {
        if (productionTime == null || expirationTime == null || !expirationTime.isAfter(productionTime)) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "expiryDate 必须晚于 productionDate");
        }
    }

    public void ensureStockNotBelowCommitted(Integer requestedStockQuantity, Integer lockedQuantity, Integer soldQuantity) {
        int committedQuantity = defaultZero(lockedQuantity) + defaultZero(soldQuantity);
        if (requestedStockQuantity == null || requestedStockQuantity < committedQuantity) {
            throw new ApplicationException(ErrorCode.CONFLICT, "库存总量不能小于已锁定加已售数量");
        }
    }

    public int calculateDaysUntilExpiry(LocalDateTime expirationTime) {
        if (expirationTime == null) {
            return 0;
        }
        return Math.max(0, (int) Duration.between(LocalDateTime.now(), expirationTime).toDays());
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
