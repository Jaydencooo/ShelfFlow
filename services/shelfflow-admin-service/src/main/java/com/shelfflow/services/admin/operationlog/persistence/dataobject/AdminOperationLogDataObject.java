package com.shelfflow.services.admin.operationlog.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOperationLogDataObject {
    private Long id;
    private Long actorId;
    private String module;
    private String action;
    private String method;
    private String path;
    private Integer statusCode;
    private String requestId;
    private String summary;
    private LocalDateTime createTime;
}
