package com.shelfflow.services.admin.inventorybatch.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.domain.BatchStatus;
import com.shelfflow.services.common.domain.PricingStatus;
import com.shelfflow.services.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BatchLifecyclePolicyTest {

    private final BatchLifecyclePolicy batchLifecyclePolicy = new BatchLifecyclePolicy();

    @Test
    void shouldDefaultWritableStatusToActive() {
        assertEquals(BatchStatus.ACTIVE, batchLifecyclePolicy.resolveWritableStatus(null));
    }

    @Test
    void shouldRejectExpiredStatusAsDirectWriteTarget() {
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> batchLifecyclePolicy.resolveWritableStatus(BatchStatus.EXPIRED)
        );

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }

    @Test
    void shouldRejectManualTransitionFromExpiredToActive() {
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> batchLifecyclePolicy.ensureManualTransitionAllowed(BatchStatus.EXPIRED, BatchStatus.ACTIVE)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
    }

    @Test
    void shouldResolvePricingStatusFromLegacyBatchStatus() {
        assertEquals(PricingStatus.DISABLED, batchLifecyclePolicy.resolvePricingStatus(0));
        assertEquals(PricingStatus.ACTIVE, batchLifecyclePolicy.resolvePricingStatus(1));
        assertEquals(PricingStatus.STALE, batchLifecyclePolicy.resolvePricingStatus(3));
    }
}
