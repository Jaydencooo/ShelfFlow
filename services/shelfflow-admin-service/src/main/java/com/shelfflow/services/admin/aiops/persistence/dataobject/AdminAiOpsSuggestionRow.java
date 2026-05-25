package com.shelfflow.services.admin.aiops.persistence.dataobject;

import lombok.Data;

@Data
public class AdminAiOpsSuggestionRow {
    private String type;
    private String priority;
    private String title;
    private String content;
    private Long productId;
    private String productName;
    private Long batchId;
    private String batchCode;
    private Integer daysToExpire;
    private Integer availableQuantity;
    private String suggestedAction;
}
