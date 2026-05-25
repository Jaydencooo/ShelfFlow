package com.shelfflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 选品车
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //名称
    private String name;

    //用户id
    private Long userId;

    //商品id
    private Long productId;

    //组合包id
    private Long bundleId;

    //库存批次id
    private Long batchId;

    //规格
    private String productSpec;

    //数量
    private Integer number;

    //金额
    private BigDecimal amount;

    //图片
    private String image;

    private LocalDateTime createTime;
}
