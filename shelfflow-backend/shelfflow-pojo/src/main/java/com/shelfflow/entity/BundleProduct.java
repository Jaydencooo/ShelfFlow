package com.shelfflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 组合包商品关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //组合包id
    private Long bundleId;

    //商品id
    private Long productId;

    //商品名称 （冗余字段）
    private String name;

    //商品原价
    private BigDecimal price;

    //份数
    private Integer copies;
}
