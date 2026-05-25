package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.api.PageQuery;
import com.shelfflow.services.common.domain.ProductStatus;
import lombok.Data;

@Data
public class ProductQuery extends PageQuery {
    private String keyword;
    private String categoryId;
    private ProductStatus status;
}
