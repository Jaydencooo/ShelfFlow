package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.api.PageQuery;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class AdminOrderEventInboxQuery extends PageQuery {

    @Size(max = 64, message = "订单号不能超过 64 位")
    private String orderNumber;

    @Size(max = 32, message = "事件类型不能超过 32 位")
    private String eventType;

    @Size(max = 16, message = "消费状态不能超过 16 位")
    private String status;
}
