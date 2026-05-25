package com.shelfflow.services.user.catalog.persistence.dataobject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCatalogProductCriteria {
    private String keyword;
    private Long categoryId;
    private String sortColumn;
    private String sortDirection;
    private int limit;
    private int offset;
}
