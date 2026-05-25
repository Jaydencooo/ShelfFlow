package com.shelfflow.services.common.dto;

import com.shelfflow.services.common.domain.ProductStatus;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ProductUpsertRequest {
    private String id;

    @NotBlank
    @Size(max = 32, message = "商品名称不能超过 32 个字符")
    private String name;

    @NotBlank
    private String categoryId;

    @NotNull
    @DecimalMin("0.01")
    private java.math.BigDecimal price;

    @Size(max = 255, message = "图片地址不能超过 255 个字符")
    private String image;

    @Size(max = 255, message = "描述不能超过 255 个字符")
    private String description;

    private ProductStatus status = ProductStatus.ACTIVE;
}
