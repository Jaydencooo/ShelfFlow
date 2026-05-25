package com.shelfflow.services.admin.pricing.persistence.dataobject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminPricingRuleCriteria {
    private int limit;
    private int offset;
    private String keyword;
    private Integer status;
    private String sortColumn;
    private String sortDirection;
}
