package com.shelfflow.services.admin.aiops.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminAiOpsSuggestionActionDataObject {
    private Long id;
    private String suggestionId;
    private String status;
    private Long actorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
