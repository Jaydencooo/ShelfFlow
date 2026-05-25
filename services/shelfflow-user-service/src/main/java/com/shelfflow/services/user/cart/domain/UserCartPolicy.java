package com.shelfflow.services.user.cart.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

@Component
public class UserCartPolicy {

    public Long parseRequiredProductId(String productId) {
        return parseRequiredLong(productId, "productId");
    }

    public Long parseOptionalBatchId(String batchId) {
        if (batchId == null || batchId.isBlank()) {
            return null;
        }
        return parseRequiredLong(batchId, "batchId");
    }

    public Long parseRequiredCartItemId(String cartItemId) {
        return parseRequiredLong(cartItemId, "cartItemId");
    }

    public int resolveQuantity(Integer quantity) {
        int resolvedQuantity = quantity == null ? 1 : quantity;
        if (resolvedQuantity < 1) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "quantity 必须大于 0");
        }
        return resolvedQuantity;
    }

    public String normalizeOptionalSpec(String productSpec) {
        if (productSpec == null) {
            return null;
        }
        String trimmed = productSpec.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public void ensureSellableSelectionExists(boolean exists) {
        if (!exists) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "商品当前不可加入购物车");
        }
    }

    public void ensureQuantityWithinAvailable(int newQuantity, Integer availableQuantity) {
        int resolvedAvailable = availableQuantity == null ? 0 : availableQuantity;
        if (newQuantity > resolvedAvailable) {
            throw new ApplicationException(ErrorCode.CONFLICT, "购物车数量超过可售库存");
        }
    }

    public void ensureOwnedCartItemRemoved(boolean removed) {
        if (!removed) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "购物车项不存在");
        }
    }

    public void ensureOwnedCartItemExists(boolean exists) {
        if (!exists) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "购物车项不存在");
        }
    }

    private Long parseRequiredLong(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 不能为空");
        }
        try {
            return Long.valueOf(rawValue.trim());
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 必须为数字");
        }
    }
}
