package com.shelfflow.services.user.order.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserOrderCartItemRow {
    private Long cartItemId;
    private Long productId;
    private Long batchId;
    private String name;
    private String image;
    private String productSpec;
    private Integer quantity;
    private BigDecimal amount;
    private Integer availableQuantity;
}
