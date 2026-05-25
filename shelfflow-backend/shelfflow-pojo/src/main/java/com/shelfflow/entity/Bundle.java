package com.shelfflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 组合包
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bundle implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //分类id
    private Long categoryId;

    //组合包名称
    private String name;

    //组合包价格
    private BigDecimal price;

    //状态 0:停用 1:启用
    private Integer status;

    //描述信息
    private String description;

    //图片
    private String image;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
