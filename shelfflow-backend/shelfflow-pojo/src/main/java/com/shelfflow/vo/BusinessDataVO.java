package com.shelfflow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDataVO implements Serializable {

    private Double flowAmount;//流转额

    private Integer validOrderCount;//有效订单数

    private Double orderCompletionRate;//订单完成率

    private Double unitPrice;//平均订单金额

    private Integer newUsers;//新增用户数

}
