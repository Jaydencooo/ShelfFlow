package com.shelfflow.services.user.cart.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCartProductSelectionRow {
    private Long productId;
    private Long batchId;
    private String productName;
    private String image;
    private BigDecimal listPrice;
    private Integer availableQuantity;
    private LocalDateTime expirationTime;
}
