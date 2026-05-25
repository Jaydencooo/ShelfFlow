package com.shelfflow.services.admin.product.persistence.dataobject;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductPageCriteria {
    Integer limit;
    Integer offset;
    String keyword;
    Long categoryId;
    Integer status;
    String sortColumn;
    String sortDirection;
}
