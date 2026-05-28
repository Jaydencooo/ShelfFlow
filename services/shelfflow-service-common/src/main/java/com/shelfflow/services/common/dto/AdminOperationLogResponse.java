package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminOperationLogResponse {
    private String id;
    private String module;
    private String action;
    private String method;
    private String path;
    private Integer statusCode;
    private Long actorId;
    private String summary;
    private LocalDateTime createTime;
}
