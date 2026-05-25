package com.shelfflow.services.user.catalog.persistence.dataobject;

import lombok.Data;

@Data
public class UserCategoryRow {
    private Long id;
    private String name;
    private Integer sort;
}
