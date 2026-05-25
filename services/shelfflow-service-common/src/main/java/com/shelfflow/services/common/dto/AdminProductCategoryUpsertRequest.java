package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AdminProductCategoryUpsertRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称不能超过 32 个字符")
    private String name;

    private Integer sort = 0;
}
