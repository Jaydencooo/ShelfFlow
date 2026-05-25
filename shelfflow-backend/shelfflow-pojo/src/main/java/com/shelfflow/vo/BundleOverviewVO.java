package com.shelfflow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 组合包总览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleOverviewVO implements Serializable {
    // 已启售数量
    private Integer sold;

    // 已停售数量
    private Integer discontinued;
}
