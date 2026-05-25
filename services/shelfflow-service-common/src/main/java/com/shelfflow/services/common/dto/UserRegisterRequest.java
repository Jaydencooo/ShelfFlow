package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UserRegisterRequest {
    @NotBlank
    @Size(min = 4, max = 64)
    private String openId;

    @NotBlank
    @Size(min = 2, max = 32)
    private String name;

    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "phone 格式不正确")
    private String phone;

    @NotBlank
    @Size(min = 8, max = 32)
    private String password;
}
