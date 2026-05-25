package com.shelfflow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBatchVO implements Serializable {

    private Long id;
    private Long productId;
    private Long categoryId;
    private String productName;
    private String batchCode;
    private LocalDateTime productionTime;
    private LocalDateTime expirationTime;
    private Integer stockQuantity;
    private Integer lockedQuantity;
    private Integer soldQuantity;
    private Integer availableQuantity;
    private Integer daysToExpire;
    private BigDecimal basePrice;
    private BigDecimal dynamicPrice;
    private Integer status;
    private LocalDateTime updateTime;
}
