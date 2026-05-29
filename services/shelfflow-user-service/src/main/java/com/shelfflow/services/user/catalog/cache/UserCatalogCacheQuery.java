package com.shelfflow.services.user.catalog.cache;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserCatalogCacheQuery {
    String keyword;
    Long categoryId;
    String sortColumn;
    String sortDirection;
    int page;
    int pageSize;
}
