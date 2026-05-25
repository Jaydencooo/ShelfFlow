package com.shelfflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 动态定价规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //规则名称
    private String name;

    //距过期最小天数
    private Integer minDaysToExpire;

    //距过期最大天数
    private Integer maxDaysToExpire;

    //折扣率，例如 0.80 表示八折
    private BigDecimal discountRate;

    //优先级，数字越大越优先
    private Integer priority;

    //状态 0停用 1启用
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
