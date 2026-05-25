package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.PricingRuleStatus;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class AdminPricingRuleUpsertRequest {
    @NotBlank
    @Size(max = 64)
    private String name;

    @NotNull
    @Min(0)
    @Max(3650)
    private Integer minDaysToExpire;

    @NotNull
    @Min(0)
    @Max(3650)
    private Integer maxDaysToExpire;

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "1.00")
    private BigDecimal discountRate;

    @NotNull
    @Min(0)
    @Max(10000)
    private Integer priority;

    private PricingRuleStatus status = PricingRuleStatus.ENABLED;
}
