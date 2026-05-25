package com.shelfflow.services.admin.inventorybatch.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.domain.BatchStatus;
import com.shelfflow.services.common.domain.PricingStatus;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

@Component
public class BatchLifecyclePolicy {

    public BatchStatus resolveWritableStatus(BatchStatus requestedStatus) {
        if (requestedStatus == null) {
            return BatchStatus.ACTIVE;
        }
        if (requestedStatus != BatchStatus.ACTIVE && requestedStatus != BatchStatus.PAUSED) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "批次状态不允许直接写入");
        }
        return requestedStatus;
    }

    public void ensureManualTransitionAllowed(BatchStatus currentStatus, BatchStatus targetStatus) {
        BatchStatus writableTarget = resolveWritableStatus(targetStatus);
        if (!isManualTransitionAllowed(currentStatus, writableTarget)) {
            throw new ApplicationException(ErrorCode.CONFLICT, "当前批次状态不允许手动流转");
        }
    }

    public PricingStatus resolvePricingStatus(Integer legacyBatchStatus) {
        if (legacyBatchStatus != null && legacyBatchStatus == 3) {
            return PricingStatus.STALE;
        }
        if (legacyBatchStatus != null && legacyBatchStatus == 0) {
            return PricingStatus.DISABLED;
        }
        return PricingStatus.ACTIVE;
    }

    private boolean isManualTransitionAllowed(BatchStatus currentStatus, BatchStatus targetStatus) {
        if ((currentStatus == BatchStatus.ACTIVE && targetStatus == BatchStatus.PAUSED)
                || (currentStatus == BatchStatus.PAUSED && targetStatus == BatchStatus.ACTIVE)) {
            return true;
        }
        return currentStatus == targetStatus;
    }
}
