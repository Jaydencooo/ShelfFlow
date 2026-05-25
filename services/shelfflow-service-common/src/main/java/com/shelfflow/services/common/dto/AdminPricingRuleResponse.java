package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.PricingRuleStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminPricingRuleResponse {
    private String id;
    private String name;
    private Integer minDaysToExpire;
    private Integer maxDaysToExpire;
    private BigDecimal discountRate;
    private Integer priority;
    private PricingRuleStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
