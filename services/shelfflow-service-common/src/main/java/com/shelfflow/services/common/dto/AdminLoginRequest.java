package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AdminLoginRequest {
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @NotBlank
    @Size(min = 6, max = 128)
    private String password;
}
