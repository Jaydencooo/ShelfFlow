package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.OrderEventActorType;
import com.shelfflow.services.common.domain.OrderEventType;
import com.shelfflow.services.common.domain.UserOrderPayStatus;
import com.shelfflow.services.common.domain.UserOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderEventResponse {
    private String id;
    private OrderEventType eventType;
    private OrderEventActorType actorType;
    private String actorId;
    private UserOrderStatus fromStatus;
    private UserOrderStatus toStatus;
    private UserOrderPayStatus fromPayStatus;
    private UserOrderPayStatus toPayStatus;
    private String note;
    private LocalDateTime eventTime;
}
