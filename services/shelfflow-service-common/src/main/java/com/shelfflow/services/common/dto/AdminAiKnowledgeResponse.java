package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminAiKnowledgeResponse {
    private String id;
    private String title;
    private String category;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
