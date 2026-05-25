package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.BatchStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BatchStatusUpdateRequest {
    @NotNull
    private BatchStatus batchStatus;
}
