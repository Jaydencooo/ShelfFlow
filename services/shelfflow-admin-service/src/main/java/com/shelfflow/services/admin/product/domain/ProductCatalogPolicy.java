package com.shelfflow.services.admin.product.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.domain.ProductStatus;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

@Component
public class ProductCatalogPolicy {

    private static final int DEFAULT_CATEGORY_SORT = 0;

    public ProductStatus resolveWritableStatus(ProductStatus status) {
        return status == null ? ProductStatus.ACTIVE : status;
    }

    public String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "商品名称不能为空");
        }
        return name.trim();
    }

    public String normalizeCategoryName(String name) {
        if (name == null || name.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "分类名称不能为空");
        }
        return name.trim();
    }

    public int normalizeCategorySort(Integer sort) {
        return sort == null ? DEFAULT_CATEGORY_SORT : sort;
    }

    public String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public void ensureCategoryExists(boolean exists) {
        if (!exists) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "商品分类不存在");
        }
    }

    public void ensureUniqueName(Long existingId, Long currentProductId) {
        if (existingId != null && !existingId.equals(currentProductId)) {
            throw new ApplicationException(ErrorCode.CONFLICT, "商品名称已存在");
        }
    }

    public void ensureUniqueCategoryName(Long existingId) {
        if (existingId != null) {
            throw new ApplicationException(ErrorCode.CONFLICT, "分类名称已存在");
        }
    }

    public void ensureCategoryCanBeDeleted(long productCount) {
        if (productCount > 0) {
            throw new ApplicationException(ErrorCode.CONFLICT, "分类下仍有商品，不能删除");
        }
    }
}
