package com.shelfflow.services.admin.order.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.domain.UserOrderPayStatus;
import com.shelfflow.services.common.domain.UserOrderStatus;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminOrderFulfillmentPolicy {

    private static final String SORT_BY_ORDER_TIME = "orderTime";
    private static final String SORT_BY_CHECKOUT_TIME = "checkoutTime";
    private static final String SORT_BY_PICKUP_DEADLINE = "pickupDeadline";
    private static final String SORT_BY_TOTAL_AMOUNT = "totalAmount";
    private static final String SORT_BY_UPDATED_AT = "updatedAt";

    public Long parseRequiredOrderId(String value) {
        if (value == null || value.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "订单 ID 不能为空");
        }
        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                throw new NumberFormatException("Order id must be positive");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "订单 ID 必须为正整数");
        }
    }

    public UserOrderStatus parseOptionalStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return UserOrderStatus.fromValue(status.trim());
        } catch (IllegalArgumentException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "订单状态不支持");
        }
    }

    public UserOrderPayStatus parseOptionalPayStatus(String payStatus) {
        if (payStatus == null || payStatus.isBlank()) {
            return null;
        }
        try {
            return UserOrderPayStatus.fromValue(payStatus.trim());
        } catch (IllegalArgumentException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "支付状态不支持");
        }
    }

    public String resolveSortColumn(String sortBy) {
        if (sortBy == null || sortBy.isBlank() || SORT_BY_UPDATED_AT.equals(sortBy) || SORT_BY_ORDER_TIME.equals(sortBy)) {
            return "o.order_time";
        }
        if (SORT_BY_CHECKOUT_TIME.equals(sortBy)) {
            return "o.checkout_time";
        }
        if (SORT_BY_PICKUP_DEADLINE.equals(sortBy)) {
            return "o.pickup_deadline";
        }
        if (SORT_BY_TOTAL_AMOUNT.equals(sortBy)) {
            return "o.amount";
        }
        throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "sortBy 不支持");
    }

    public void ensureOrderExists(boolean exists) {
        if (!exists) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "订单不存在");
        }
    }

    public void ensureAdminTransitionAllowed(UserOrderStatus currentStatus, UserOrderStatus targetStatus, UserOrderPayStatus payStatus) {
        if (payStatus != UserOrderPayStatus.PAID) {
            throw new ApplicationException(ErrorCode.CONFLICT, "订单未支付，不能进入履约流程");
        }
        if (targetStatus == UserOrderStatus.PREPARING && currentStatus == UserOrderStatus.TO_PREPARE) {
            return;
        }
        if (targetStatus == UserOrderStatus.READY_FOR_PICKUP && currentStatus == UserOrderStatus.PREPARING) {
            return;
        }
        if (targetStatus == UserOrderStatus.COMPLETED && currentStatus == UserOrderStatus.READY_FOR_PICKUP) {
            return;
        }
        throw new ApplicationException(ErrorCode.CONFLICT, "订单状态流转不合法");
    }

    public void ensurePickupVerificationAllowed(UserOrderStatus currentStatus,
                                                UserOrderPayStatus payStatus,
                                                String expectedPickupCode,
                                                String submittedPickupCode,
                                                LocalDateTime pickupDeadline,
                                                LocalDateTime verifyTime) {
        if (currentStatus != UserOrderStatus.READY_FOR_PICKUP) {
            throw new ApplicationException(ErrorCode.CONFLICT, "只有待自提订单可以核销");
        }
        if (payStatus != UserOrderPayStatus.PAID) {
            throw new ApplicationException(ErrorCode.CONFLICT, "订单未支付，不能核销自提");
        }
        if (expectedPickupCode == null || expectedPickupCode.isBlank()) {
            throw new ApplicationException(ErrorCode.CONFLICT, "订单缺少自提码，不能核销");
        }
        if (!expectedPickupCode.equalsIgnoreCase(submittedPickupCode.trim())) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "自提码不匹配");
        }
        if (pickupDeadline != null && pickupDeadline.isBefore(verifyTime)) {
            throw new ApplicationException(ErrorCode.CONFLICT, "订单已超过自提截止时间，请先人工确认后处理");
        }
    }

    public boolean requiresInventorySettlement(UserOrderStatus targetStatus) {
        return targetStatus == UserOrderStatus.COMPLETED;
    }

    public void ensureInventorySettled(boolean settled) {
        if (!settled) {
            throw new ApplicationException(ErrorCode.CONFLICT, "订单库存结转失败");
        }
    }
}
