package com.shelfflow.services.common.api;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class PageQuery {
    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int pageSize = 20;

    private String sortBy = "updatedAt";
    private SortOrder sortOrder = SortOrder.DESC;
}
