package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PickupPointUpsertRequest {
    @NotBlank(message = "自提点名称不能为空")
    @Size(max = 64, message = "自提点名称不能超过 64 个字符")
    private String name;

    @NotBlank(message = "自提点地址不能为空")
    @Size(max = 160, message = "自提点地址不能超过 160 个字符")
    private String address;

    @Size(max = 32, message = "联系人不能超过 32 个字符")
    private String contactName;

    @Size(max = 32, message = "联系电话不能超过 32 个字符")
    private String contactPhone;

    @Size(max = 80, message = "服务时间不能超过 80 个字符")
    private String serviceTime;

    private Integer sort;

    private Boolean enabled;
}
