package com.shelfflow.services.admin.aiops.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminAiOpsSuggestionActionLogDataObject {
    private Long id;
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
