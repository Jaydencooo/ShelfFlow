package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.BatchStatus;
import com.shelfflow.services.common.domain.PricingStatus;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class InventoryBatchUpsertRequest {
    private String id;

    @NotBlank
    private String productId;

    private String batchCode;

    @NotBlank
    private String productionDate;

    @NotBlank
    private String expiryDate;

    @NotNull
    @Min(1)
    private Integer stockQuantity;

    @NotNull
    @DecimalMin("0.01")
    private java.math.BigDecimal basePrice;

    private BatchStatus batchStatus = BatchStatus.ACTIVE;

    private PricingStatus pricingStatus = PricingStatus.ACTIVE;
}
