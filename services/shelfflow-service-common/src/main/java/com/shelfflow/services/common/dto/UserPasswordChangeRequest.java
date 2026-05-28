package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserPasswordChangeRequest {
    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
