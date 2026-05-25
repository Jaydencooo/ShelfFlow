package com.shelfflow.services.user.catalog.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserCatalogPricingRuleRow {
    private Long id;
    private Integer minDaysToExpire;
    private Integer maxDaysToExpire;
    private BigDecimal discountRate;
    private Integer priority;
}
