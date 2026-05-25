package com.shelfflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders implements Serializable {

    /**
     * 订单状态 1待付款 2待备货 3备货中 4待自提/待核销 5已完成 6已取消
     */
    public static final Integer PENDING_PAYMENT = 1;
    public static final Integer TO_BE_CONFIRMED = 2;
    public static final Integer CONFIRMED = 3;
    public static final Integer READY_FOR_PICKUP = 4;
    public static final Integer COMPLETED = 5;
    public static final Integer CANCELLED = 6;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;

    private static final long serialVersionUID = 1L;

    private Long id;

    //订单号
    private String number;

    //订单状态 1待付款 2待备货 3备货中 4待自提/待核销 5已完成 6已取消 7退款
    private Integer status;

    //下单用户id
    private Long userId;

    //自提联系人id，自提订单可为空
    private Long pickupContactId;

    //下单时间
    private LocalDateTime orderTime;

    //结账时间
    private LocalDateTime checkoutTime;

    //支付方式 1微信，2支付宝
    private Integer payMethod;

    //支付状态 0未支付 1已支付 2退款
    private Integer payStatus;

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

    //订单取消原因
    private String cancelReason;

    //订单拒绝原因
    private String rejectionReason;

    //订单取消时间
    private LocalDateTime cancelTime;

    //履约准备模式 1立即备货 0预约自提
    private Integer preparationMode;

    //订单完成时间
    private LocalDateTime completedTime;

    //履约方式 1平台履约 2到店自提
    private Integer fulfillmentType;

    //自提核销码
    private String pickupCode;

    //预约自提时间
    private LocalDateTime pickupTime;

    //自提截止时间
    private LocalDateTime pickupDeadline;

    //核销时间
    private LocalDateTime verifyTime;

    //核销运营人员id
    private Long verifyStaffId;

    //履约服务费
    private int fulfillmentFee;

    //履约包装数量
    private int packageCount;

    //履约包装策略  1按商品数量提供  0选择具体数量
    private Integer packageStrategy;
}
