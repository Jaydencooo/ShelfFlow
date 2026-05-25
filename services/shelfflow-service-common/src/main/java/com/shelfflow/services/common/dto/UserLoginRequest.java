package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserLoginRequest {
    @NotBlank
    @Size(max = 64)
    private String openId;

    @NotBlank
    @Size(min = 8, max = 32)
    private String password;
}
