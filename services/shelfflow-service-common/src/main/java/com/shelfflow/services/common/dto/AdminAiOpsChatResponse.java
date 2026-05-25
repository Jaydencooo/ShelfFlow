package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminAiOpsChatResponse {
    private String sessionId;
    private String provider;
    private String model;
    private String answer;
    private List<String> references;
}
