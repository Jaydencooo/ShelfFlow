package com.shelfflow.vo;

import com.shelfflow.entity.ProductSpec;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO implements Serializable {

    private Long id;
    //商品名称
    private String name;
    //商品分类id
    private Long categoryId;
    //商品价格
    private BigDecimal price;
    //图片
    private String image;
    //描述信息
    private String description;
    //0 停售 1 起售
    private Integer status;
    //更新时间
    private LocalDateTime updateTime;
    //分类名称
    private String categoryName;
    //商品关联的规格
    private List<ProductSpec> flavors = new ArrayList<>();

    //推荐购买批次id
    private Long batchId;
    //最近过期时间
    private LocalDateTime nearestExpirationTime;
    //距过期天数
    private Integer daysToExpire;
    //可售库存
    private Integer availableQuantity;
    //动态成交价
    private BigDecimal dynamicPrice;

    //private Integer copies;
}
