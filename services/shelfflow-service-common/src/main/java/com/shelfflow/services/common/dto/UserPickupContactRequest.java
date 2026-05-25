package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UserPickupContactRequest {
    @NotBlank
    @Size(min = 2, max = 32)
    private String consignee;

    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "phone 格式不正确")
    private String phone;

    @Size(max = 16)
    private String label;

    @Size(max = 120)
    private String detail;

    private Boolean defaultContact;
}
