package com.shelfflow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingRuleVO implements Serializable {

    private Long id;
    private String name;
    private Integer minDaysToExpire;
    private Integer maxDaysToExpire;
    private BigDecimal discountRate;
    private Integer priority;
    private Integer status;
    private LocalDateTime updateTime;
}
