package com.shelfflow.services.user.order.persistence.dataobject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserOrderPageCriteria {
    private Long userId;
    private Integer status;
    private int offset;
    private int pageSize;
    private String sortColumn;
    private String sortOrder;
}
