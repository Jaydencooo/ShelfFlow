package com.shelfflow.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InventoryBatchPageQueryDTO implements Serializable {

    private int page;
    private int pageSize;
    private Long productId;
    private Long categoryId;
    private String batchCode;
    private Integer status;
}
