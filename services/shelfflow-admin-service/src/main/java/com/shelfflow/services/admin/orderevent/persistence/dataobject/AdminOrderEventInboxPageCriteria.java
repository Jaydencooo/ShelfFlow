package com.shelfflow.services.admin.orderevent.persistence.dataobject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminOrderEventInboxPageCriteria {
    private String orderNumber;
    private String eventType;
    private String status;
    private int offset;
    private int pageSize;
}
