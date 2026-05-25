package com.shelfflow.services.admin.product.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductCategoryDataObject {
    private Long id;
    private Integer type;
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
