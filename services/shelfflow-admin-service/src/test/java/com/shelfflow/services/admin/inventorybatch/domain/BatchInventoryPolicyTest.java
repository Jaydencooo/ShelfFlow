package com.shelfflow.services.admin.inventorybatch.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchInventoryPolicyTest {

    private final BatchInventoryPolicy batchInventoryPolicy = new BatchInventoryPolicy();

    @Test
    void shouldRejectExpiryBeforeProduction() {
        LocalDateTime productionTime = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime expirationTime = LocalDateTime.of(2026, 5, 19, 10, 0);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> batchInventoryPolicy.validateDateRange(productionTime, expirationTime)
        );

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }

    @Test
    void shouldRejectRequestedStockBelowCommittedQuantity() {
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> batchInventoryPolicy.ensureStockNotBelowCommitted(3, 2, 2)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
    }

    @Test
    void shouldReturnNonNegativeRemainingDays() {
        int days = batchInventoryPolicy.calculateDaysUntilExpiry(LocalDateTime.now().plusDays(3));
        assertTrue(days >= 2);
    }
}
