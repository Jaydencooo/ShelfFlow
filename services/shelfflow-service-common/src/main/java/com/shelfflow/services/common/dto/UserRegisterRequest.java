package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserRegisterRequest {
    @Size(max = 64)
    private String account;

    @Size(min = 4, max = 64)
    private String openId;

    @NotBlank
    @Size(min = 2, max = 32)
    private String name;

    @Size(max = 11)
    private String phone;

    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 8, max = 32)
    private String password;

    @NotBlank
    @Size(min = 8, max = 32)
    private String confirmPassword;

    @NotBlank
    @Size(min = 4, max = 10)
    private String verificationCode;
}
