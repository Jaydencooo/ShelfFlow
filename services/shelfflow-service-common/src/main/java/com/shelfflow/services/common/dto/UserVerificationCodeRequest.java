package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserVerificationCodeRequest {
    @NotBlank
    @Size(max = 100)
    private String account;

    @NotBlank
    @Size(max = 32)
    private String purpose;
}
