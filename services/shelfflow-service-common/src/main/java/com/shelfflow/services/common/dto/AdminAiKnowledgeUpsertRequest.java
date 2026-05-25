package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AdminAiKnowledgeUpsertRequest {
    @NotBlank
    @Size(max = 80)
    private String title;

    @NotBlank
    @Size(max = 32)
    private String category;

    @NotBlank
    @Size(max = 4000)
    private String content;
}
