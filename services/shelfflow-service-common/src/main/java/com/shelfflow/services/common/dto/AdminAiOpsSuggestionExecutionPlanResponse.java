package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAiOpsSuggestionExecutionPlanResponse {
    private String targetType;
    private String targetId;
    private String targetName;
    private String operationType;
    private String defaultBatchStatus;
    private String summary;
    private String editableFields;
}
