package com.shelfflow.services.admin.inventorybatch.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryBatchDataObject {
    private Long id;
    private Long productId;
    private String batchCode;
    private LocalDateTime productionTime;
    private LocalDateTime expirationTime;
    private Integer stockQuantity;
    private Integer lockedQuantity;
    private Integer soldQuantity;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
