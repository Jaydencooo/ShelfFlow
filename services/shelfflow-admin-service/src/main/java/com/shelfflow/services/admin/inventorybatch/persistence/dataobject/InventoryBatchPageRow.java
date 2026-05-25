package com.shelfflow.services.admin.inventorybatch.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryBatchPageRow {
    private Long id;
    private Long productId;
    private String productName;
    private Long categoryId;
    private String batchCode;
    private LocalDateTime productionTime;
    private LocalDateTime expirationTime;
    private Integer stockQuantity;
    private Integer lockedQuantity;
    private Integer soldQuantity;
    private Integer availableQuantity;
    private Integer daysToExpire;
    private Integer wasteQuantity;
    private BigDecimal basePrice;
    private BigDecimal currentPrice;
    private Integer status;
}
