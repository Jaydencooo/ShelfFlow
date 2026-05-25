package com.shelfflow.services.admin.product.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductPageRow {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private Integer daysToExpire;
}
