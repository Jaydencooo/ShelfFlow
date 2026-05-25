package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.BatchStatus;
import com.shelfflow.services.common.domain.PricingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InventoryBatchRecordResponse {
    private String id;
    private String productId;
    private String productName;
    private String categoryId;
    private String batchCode;
    private String productionDate;
    private String expiryDate;
    private Integer shelfLifeDays;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer soldStock;
    private Integer wasteStock;
    private BigDecimal basePrice;
    private BigDecimal currentPrice;
    private BatchStatus batchStatus;
    private PricingStatus pricingStatus;
}
