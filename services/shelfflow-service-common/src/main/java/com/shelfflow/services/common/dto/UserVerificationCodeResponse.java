package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVerificationCodeResponse {
    private String target;
    private String purpose;
    private Integer expiresInSeconds;
    private String debugCode;
}
