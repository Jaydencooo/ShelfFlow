package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminAiOpsSuggestionActionResponse {
    private String id;
    private String suggestionId;
    private String action;
    private String status;
    private String targetType;
    private String targetId;
    private String targetName;
    private String operationSummary;
    private String operationPayload;
    private Long actorId;
    private LocalDateTime createTime;
}
