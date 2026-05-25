package com.shelfflow.services.admin.inventorybatch.persistence.dataobject;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InventoryBatchPageCriteria {
    Integer limit;
    Integer offset;
    String keyword;
    Long categoryId;
    Integer status;
    String pricingStatus;
    Integer expiryDaysMin;
    Integer expiryDaysMax;
    String sortColumn;
    String sortDirection;
}
