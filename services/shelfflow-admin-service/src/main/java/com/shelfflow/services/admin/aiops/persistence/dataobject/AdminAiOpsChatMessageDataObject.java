package com.shelfflow.services.admin.aiops.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminAiOpsChatMessageDataObject {
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private String provider;
    private String model;
    private String referencesJson;
    private LocalDateTime createTime;
}
