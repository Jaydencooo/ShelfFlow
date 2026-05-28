package com.shelfflow.services.user.order.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.domain.UserOrderPayStatus;
import com.shelfflow.services.common.domain.UserOrderStatus;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.user.config.UserOrderProperties;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderCartItemRow;
import com.shelfflow.services.user.pickuppoint.persistence.dataobject.UserPickupPointDataObject;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class UserOrderPolicy {

    private static final DateTimeFormatter ORDER_NUMBER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int ORDER_NUMBER_RANDOM_BOUND = 1000;
    private static final int MIN_PICKUP_CODE_LENGTH = 4;
    private static final int MAX_PICKUP_CODE_LENGTH = 12;
    private static final int MAX_CANCEL_REASON_LENGTH = 100;
    private static final int MAX_SUBMIT_CART_ITEM_COUNT = 100;
    private static final String DEFAULT_CANCEL_REASON = "用户取消订单";

    private final UserOrderProperties userOrderProperties;

    public UserOrderPolicy(UserOrderProperties userOrderProperties) {
        this.userOrderProperties = userOrderProperties;
    }

    public String normalizeOptionalRemark(String remark) {
        if (remark == null) {
            return null;
        }
        String trimmed = remark.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UserOrderStatus parseOptionalStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return UserOrderStatus.fromValue(status.trim());
        } catch (IllegalArgumentException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "status 不支持");
        }
    }

    public String resolveSortColumn(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "o.order_time";
        }
        return switch (sortBy) {
            case "updatedAt", "orderTime" -> "o.order_time";
            case "amount" -> "o.amount";
            default -> throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "sortBy 不支持");
        };
    }

    public void ensureCartNotEmpty(List<UserOrderCartItemRow> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new ApplicationException(ErrorCode.CONFLICT, "购物车为空，无法提交订单");
        }
    }

    public void ensureCartItemsEligibleForSubmit(List<UserOrderCartItemRow> cartItems) {
        for (UserOrderCartItemRow cartItem : cartItems) {
            if (cartItem.getAvailableQuantity() == null || cartItem.getAvailableQuantity() < cartItem.getQuantity()) {
                throw new ApplicationException(ErrorCode.CONFLICT, "购物车商品库存不足，请刷新后重试");
            }
        }
    }

    public List<Long> parseOptionalCartItemIds(List<String> cartItemIds) {
        if (cartItemIds == null) {
            return null;
        }
        if (cartItemIds.isEmpty()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "请选择要结算的商品");
        }
        if (cartItemIds.size() > MAX_SUBMIT_CART_ITEM_COUNT) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "单次结算商品过多，请分批提交");
        }
        return cartItemIds.stream()
                .map(this::parseRequiredCartItemId)
                .distinct()
                .toList();
    }

    public void ensureSelectedCartItemsMatched(int expectedCount, int actualCount) {
        if (expectedCount != actualCount) {
            throw new ApplicationException(ErrorCode.CONFLICT, "部分购物车商品已失效，请刷新后重试");
        }
    }

    private Long parseRequiredCartItemId(String cartItemId) {
        if (cartItemId == null || cartItemId.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "购物车商品 ID 不能为空");
        }
        try {
            return Long.valueOf(cartItemId.trim());
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "购物车商品 ID 必须为数字");
        }
    }

    public void ensureStockLocked(boolean locked) {
        if (!locked) {
            throw new ApplicationException(ErrorCode.CONFLICT, "库存锁定失败，请刷新后重试");
        }
    }

    public void ensureStockReleased(boolean released) {
        if (!released) {
            throw new ApplicationException(ErrorCode.CONFLICT, "库存释放失败，请稍后重试");
        }
    }

    public Long parseRequiredOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "orderId 不能为空");
        }
        try {
            return Long.valueOf(orderId.trim());
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "orderId 必须为数字");
        }
    }

    public void ensureOrderExists(boolean exists) {
        if (!exists) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "订单不存在");
        }
    }

    public void ensureCancelableStatus(UserOrderStatus status) {
        if (status != UserOrderStatus.PENDING_PAYMENT && status != UserOrderStatus.TO_PREPARE) {
            throw new ApplicationException(ErrorCode.CONFLICT, "当前订单状态不允许取消");
        }
    }

    public String normalizeCancelReason(String cancelReason) {
        if (cancelReason == null) {
            return DEFAULT_CANCEL_REASON;
        }
        String trimmed = cancelReason.trim();
        if (trimmed.isEmpty()) {
            return DEFAULT_CANCEL_REASON;
        }
        if (trimmed.length() > MAX_CANCEL_REASON_LENGTH) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "取消原因不能超过 100 个字符");
        }
        return trimmed;
    }

    public void ensurePayableStatus(UserOrderStatus status, UserOrderPayStatus payStatus) {
        if (status != UserOrderStatus.PENDING_PAYMENT || payStatus != UserOrderPayStatus.UNPAID) {
            throw new ApplicationException(ErrorCode.CONFLICT, "当前订单状态不允许支付");
        }
    }

    public BigDecimal calculateOrderAmount(List<UserOrderCartItemRow> cartItems) {
        BigDecimal total = BigDecimal.ZERO;
        for (UserOrderCartItemRow cartItem : cartItems) {
            total = total.add(cartItem.getAmount().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        return total;
    }

    public int calculateItemCount(List<UserOrderCartItemRow> cartItems) {
        return cartItems.stream().mapToInt(UserOrderCartItemRow::getQuantity).sum();
    }

    public String nextOrderNumber(LocalDateTime now) {
        int suffix = ThreadLocalRandom.current().nextInt(ORDER_NUMBER_RANDOM_BOUND);
        return userOrderProperties.getOrderNumberPrefix()
                + ORDER_NUMBER_TIME_FORMATTER.format(now)
                + String.format("%03d", suffix);
    }

    public String nextPickupCode() {
        int length = userOrderProperties.getPickupCodeLength();
        if (length < MIN_PICKUP_CODE_LENGTH || length > MAX_PICKUP_CODE_LENGTH) {
            throw new ApplicationException(ErrorCode.INTERNAL_ERROR, "自提码长度配置非法");
        }
        int lowerBound = (int) Math.pow(10, length - 1);
        int upperBound = (int) Math.pow(10, length);
        return String.valueOf(ThreadLocalRandom.current().nextInt(lowerBound, upperBound));
    }

    public LocalDateTime resolvePickupDeadline(LocalDateTime orderTime) {
        return orderTime.plusHours(userOrderProperties.getPickupDeadlineHours());
    }

    public Long parseOptionalPickupPointId(String pickupPointId) {
        if (pickupPointId == null || pickupPointId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(pickupPointId.trim());
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "pickupPointId 必须为数字");
        }
    }

    public void ensurePickupPointExists(boolean exists) {
        if (!exists) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "自提点不存在或已停用");
        }
    }

    public void ensurePickupConsigneePresent(String consignee) {
        if (consignee == null || consignee.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "自提人不能为空，请先完善自提信息");
        }
    }

    public void ensurePickupPhonePresent(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "自提联系电话不能为空，请先完善自提信息");
        }
    }

    public String resolvePickupPoint(UserPickupPointDataObject pickupPoint) {
        if (pickupPoint != null) {
            return pickupPoint.getName() + "｜" + pickupPoint.getAddress();
        }
        String pickupPointName = userOrderProperties.getPickupPoint();
        if (pickupPointName == null || pickupPointName.isBlank()) {
            throw new ApplicationException(ErrorCode.INTERNAL_ERROR, "默认自提点未配置");
        }
        return pickupPointName.trim();
    }

    public String resolvePickupPoint() {
        String pickupPoint = userOrderProperties.getPickupPoint();
        if (pickupPoint == null || pickupPoint.isBlank()) {
            throw new ApplicationException(ErrorCode.INTERNAL_ERROR, "默认自提点未配置");
        }
        return pickupPoint.trim();
    }
}
