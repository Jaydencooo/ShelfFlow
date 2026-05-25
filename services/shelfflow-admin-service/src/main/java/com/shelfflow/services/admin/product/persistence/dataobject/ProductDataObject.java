package com.shelfflow.services.admin.product.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDataObject {
    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
