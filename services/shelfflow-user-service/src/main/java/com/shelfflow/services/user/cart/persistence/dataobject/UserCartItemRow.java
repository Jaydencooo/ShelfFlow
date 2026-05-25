package com.shelfflow.services.user.cart.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCartItemRow {
    private Long id;
    private Long productId;
    private Long batchId;
    private String name;
    private String image;
    private String productSpec;
    private Integer number;
    private BigDecimal amount;
    private Integer availableQuantity;
    private LocalDateTime expirationTime;
    private LocalDateTime createTime;
}
