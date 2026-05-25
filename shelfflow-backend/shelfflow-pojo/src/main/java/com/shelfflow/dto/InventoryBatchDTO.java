package com.shelfflow.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InventoryBatchDTO implements Serializable {

    private Long id;
    private Long productId;
    private String batchCode;
    private LocalDateTime productionTime;
    private LocalDateTime expirationTime;
    private Integer stockQuantity;
    private Integer lockedQuantity;
    private Integer soldQuantity;
    private Integer status;
}
