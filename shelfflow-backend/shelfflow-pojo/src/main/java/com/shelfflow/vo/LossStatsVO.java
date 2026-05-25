package com.shelfflow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 损耗统计总览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LossStatsVO implements Serializable {

    // 过期批次数
    private Integer expiredBatchCount;

    // 过期库存
    private Long expiredStockQuantity;

    // 3天内临期批次数
    private Integer expiringSoonBatchCount;

    // 3天内临期库存
    private Long expiringSoonStockQuantity;

    // 售罄批次数
    private Integer soldOutBatchCount;
}
