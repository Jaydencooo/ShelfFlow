package com.shelfflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductFlowDTO implements Serializable {
    //商品名称
    private String name;

    //流转量
    private Integer number;
}
