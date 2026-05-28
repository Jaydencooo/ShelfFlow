package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserPasswordResetRequest {
    @Size(max = 100)
    private String account;

    @Size(max = 64)
    private String openId;

    @Size(max = 11)
    private String phone;

    @NotBlank
    @Size(min = 8, max = 32)
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 32)
    private String confirmPassword;

    @NotBlank
    @Size(min = 4, max = 10)
    private String verificationCode;
}
