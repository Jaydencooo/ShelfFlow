package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.api.PageQuery;
import com.shelfflow.services.common.domain.BatchStatus;
import com.shelfflow.services.common.domain.PricingStatus;
import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class InventoryBatchQuery extends PageQuery {
    private String keyword;
    private String categoryId;
    private BatchStatus batchStatus;
    private PricingStatus pricingStatus;

    @Min(0)
    private Integer expiryDaysMin;

    @Min(0)
    private Integer expiryDaysMax;
}
