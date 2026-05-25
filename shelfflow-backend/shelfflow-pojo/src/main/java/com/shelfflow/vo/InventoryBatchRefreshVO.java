package com.shelfflow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBatchRefreshVO implements Serializable {

    private Integer expiredCount;

    private Integer soldOutCount;

    private Integer saleableCount;
}
