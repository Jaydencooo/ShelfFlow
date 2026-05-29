package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserPaymentCallbackRequest {
    @NotBlank
    @Size(max = 64)
    private String paymentNo;

    @NotBlank
    @Size(max = 64)
    private String externalTradeNo;

    @NotBlank
    @Size(max = 128)
    private String callbackEventId;

    @NotBlank
    @Size(max = 32)
    private String provider;

    @NotBlank
    @Size(max = 32)
    private String status;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private LocalDateTime paidTime;
}
