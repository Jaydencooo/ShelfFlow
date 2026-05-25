package com.shelfflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存批次
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //商品id
    private Long productId;

    //批次编号
    private String batchCode;

    //生产时间
    private LocalDateTime productionTime;

    //过期时间
    private LocalDateTime expirationTime;

    //批次总库存
    private Integer stockQuantity;

    //已锁定库存
    private Integer lockedQuantity;

    //已售库存
    private Integer soldQuantity;

    //状态 0停用 1可售 2售罄 3过期
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
