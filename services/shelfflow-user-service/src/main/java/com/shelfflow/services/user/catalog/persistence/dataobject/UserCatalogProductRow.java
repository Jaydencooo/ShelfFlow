package com.shelfflow.services.user.catalog.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCatalogProductRow {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private String image;
    private String description;
    private Integer availableQuantity;
    private Long recommendedBatchId;
    private LocalDateTime nearestExpirationTime;
}
