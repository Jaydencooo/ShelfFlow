package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminProductCategoryResponse {
    private String id;
    private String name;
    private Integer sort;
    private Integer productCount;
}
