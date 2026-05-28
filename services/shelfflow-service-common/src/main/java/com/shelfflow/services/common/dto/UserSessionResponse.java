package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSessionResponse {
    private String userId;
    private String openId;
    private String account;
    private String name;
    private String phone;
    private String email;
    private String token;
}
