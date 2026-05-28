package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserProfileUpdateRequest {
    @NotBlank
    @Size(min = 2, max = 32)
    private String name;

    @Size(max = 11)
    private String phone;

    @Size(max = 100)
    private String email;

    private String phoneVerificationCode;

    private String emailVerificationCode;
}
