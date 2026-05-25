package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAiOpsSuggestionResponse {
    private String id;
    private String type;
    private String priority;
    private String title;
    private String content;
    private String productId;
    private String productName;
    private String batchId;
    private String batchCode;
    private Integer daysToExpire;
    private Integer availableQuantity;
    private String suggestedAction;
    private String status;
}
