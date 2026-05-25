package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.api.PageQuery;
import lombok.Data;

@Data
public class UserCatalogProductQuery extends PageQuery {
    private String keyword;
    private String categoryId;
}
