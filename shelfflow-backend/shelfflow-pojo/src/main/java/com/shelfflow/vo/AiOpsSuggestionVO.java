package com.shelfflow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiOpsSuggestionVO implements Serializable {

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
