package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AdminAiOpsSuggestionActionRequest {
    @NotBlank(message = "操作不能为空")
    private String action;
}
