package com.shelfflow.services.admin.aiops.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminAiKnowledgeDataObject {
    private Long id;
    private String title;
    private String category;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
