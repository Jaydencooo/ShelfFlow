package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.PricingRuleStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AdminPricingRuleStatusUpdateRequest {
    @NotNull
    private PricingRuleStatus status;
}
