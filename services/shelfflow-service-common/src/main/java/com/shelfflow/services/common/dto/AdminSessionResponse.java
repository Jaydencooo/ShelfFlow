package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminSessionResponse {
    private String userId;
    private String username;
    private String displayName;
    private List<String> roles;
    private List<String> permissions;
    private String token;
}
