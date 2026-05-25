package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.api.PageQuery;
import com.shelfflow.services.common.domain.PricingRuleStatus;
import lombok.Data;

@Data
public class AdminPricingRuleQuery extends PageQuery {
    private String keyword;
    private PricingRuleStatus status;
}
