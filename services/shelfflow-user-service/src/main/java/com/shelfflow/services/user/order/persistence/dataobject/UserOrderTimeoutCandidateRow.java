package com.shelfflow.services.user.order.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserOrderTimeoutCandidateRow {
    private Long id;
    private String number;
    private Integer status;
    private Long userId;
    private Integer payStatus;
    private BigDecimal amount;
    private LocalDateTime orderTime;
}
