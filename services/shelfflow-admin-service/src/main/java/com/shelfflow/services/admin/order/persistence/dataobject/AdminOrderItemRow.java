package com.shelfflow.services.admin.order.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminOrderItemRow {
    private Long productId;
    private Long batchId;
    private String name;
    private String image;
    private String productSpec;
    private Integer number;
    private BigDecimal amount;
}
