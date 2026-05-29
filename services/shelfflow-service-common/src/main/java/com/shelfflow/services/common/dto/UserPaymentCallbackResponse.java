package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPaymentCallbackResponse {
    private String paymentNo;
    private String orderId;
    private String orderNumber;
    private String externalTradeNo;
    private String callbackEventId;
    private String orderStatus;
    private String payStatus;
    private boolean duplicate;
}
