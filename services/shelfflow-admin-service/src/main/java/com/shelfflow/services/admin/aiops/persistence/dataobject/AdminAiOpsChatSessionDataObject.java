package com.shelfflow.services.admin.aiops.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminAiOpsChatSessionDataObject {
    private Long id;
    private Long adminUserId;
    private String title;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
