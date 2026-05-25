package com.shelfflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersSubmitDTO implements Serializable {
    //自提联系人id，自提订单可为空
    private Long pickupContactId;
    //付款方式
    private int payMethod;
    //备注
    private String remark;
    //履约准备模式 1立即备货 0预约自提
    private Integer preparationMode;
    //履约包装数量
    private Integer packageCount;
    //履约包装策略 1按商品数量提供 0选择具体数量
    private Integer packageStrategy;
    //履约服务费
    private Integer fulfillmentFee;
    //总金额
    private BigDecimal amount;
    //履约方式 1平台履约 2到店自提
    private Integer fulfillmentType;
    //预约自提时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickupTime;
}
