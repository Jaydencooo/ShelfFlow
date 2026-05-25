package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminAiOpsChatMessageResponse {
    private String id;
    private String sessionId;
    private String role;
    private String content;
    private String provider;
    private String model;
    private List<String> references;
    private LocalDateTime createTime;
}
