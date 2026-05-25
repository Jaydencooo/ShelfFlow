package com.shelfflow.services.admin.pricing.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminPricingRuleDataObject {
    private Long id;
    private String name;
    private Integer minDaysToExpire;
    private Integer maxDaysToExpire;
    private BigDecimal discountRate;
    private Integer priority;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
