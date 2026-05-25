package com.shelfflow.services.admin.order.persistence.dataobject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminOrderPageCriteria {
    private String keyword;
    private Integer status;
    private Integer payStatus;
    private int offset;
    private int pageSize;
    private String sortColumn;
    private String sortOrder;
}
