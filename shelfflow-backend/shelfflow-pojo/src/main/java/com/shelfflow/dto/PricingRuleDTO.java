package com.shelfflow.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PricingRuleDTO implements Serializable {

    private Long id;
    private String name;
    private Integer minDaysToExpire;
    private Integer maxDaysToExpire;
    private BigDecimal discountRate;
    private Integer priority;
    private Integer status;
}
