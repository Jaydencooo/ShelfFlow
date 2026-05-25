package com.shelfflow.services.admin.product.persistence.dataobject;

import lombok.Data;

@Data
public class ProductCategoryRow {
    private Long id;
    private String name;
    private Integer sort;
    private Integer productCount;
}
