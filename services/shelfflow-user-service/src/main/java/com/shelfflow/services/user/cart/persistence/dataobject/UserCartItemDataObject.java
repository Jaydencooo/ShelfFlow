package com.shelfflow.services.user.cart.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCartItemDataObject {
    private Long id;
    private Long userId;
    private Long productId;
    private Long batchId;
    private String name;
    private String image;
    private String productSpec;
    private Integer number;
    private BigDecimal amount;
    private LocalDateTime createTime;
}
