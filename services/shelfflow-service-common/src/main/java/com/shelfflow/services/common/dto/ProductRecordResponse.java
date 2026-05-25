package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductRecordResponse {
    private String id;
    private String name;
    private String categoryId;
    private String categoryName;
    private BigDecimal price;
    private String image;
    private String description;
    private ProductStatus status;
    private Integer shelfLifeDays;
}
