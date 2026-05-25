package com.shelfflow.dto;

import com.shelfflow.entity.OrderDetail;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdersDTO implements Serializable {

    private Long id;

    //订单号
    private String number;

    //订单状态 1待付款，2待备货，3已备货，4已完成，5已取消
    private Integer status;

    //下单用户id
    private Long userId;

    //自提联系人id
    private Long pickupContactId;

    //下单时间
    private LocalDateTime orderTime;

    //结账时间
    private LocalDateTime checkoutTime;

    //支付方式 1微信，2支付宝
    private Integer payMethod;

    //实收金额
    private BigDecimal amount;

    //备注
    private String remark;

    //用户名
    private String userName;

    //手机号
    private String phone;

    //自提履约点
    private String pickupPoint;

    //联系人
    private String consignee;

    private List<OrderDetail> orderDetails;

}
