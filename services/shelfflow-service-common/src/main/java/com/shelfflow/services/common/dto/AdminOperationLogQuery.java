package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Data
public class AdminOperationLogQuery {

    @Min(value = 1, message = "page 必须大于 0")
    private int page = 1;

    @Min(value = 1, message = "pageSize 必须大于 0")
    @Max(value = 100, message = "pageSize 不能超过 100")
    private int pageSize = 20;

    @Size(max = 64, message = "模块名称不能超过 64 位")
    private String module;

    @Size(max = 64, message = "操作类型不能超过 64 位")
    private String action;
}
