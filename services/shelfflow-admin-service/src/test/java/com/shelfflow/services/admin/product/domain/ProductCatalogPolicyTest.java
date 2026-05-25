package com.shelfflow.services.admin.product.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.domain.ProductStatus;
import com.shelfflow.services.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductCatalogPolicyTest {

    private final ProductCatalogPolicy productCatalogPolicy = new ProductCatalogPolicy();

    @Test
    void shouldDefaultWritableStatusToActive() {
        assertEquals(ProductStatus.ACTIVE, productCatalogPolicy.resolveWritableStatus(null));
    }

    @Test
    void shouldNormalizeNameByTrimmingWhitespace() {
        assertEquals("Fresh Milk", productCatalogPolicy.normalizeName("  Fresh Milk  "));
    }

    @Test
    void shouldRejectBlankProductName() {
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> productCatalogPolicy.normalizeName("   ")
        );

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }

    @Test
    void shouldNormalizeEmptyOptionalTextToNull() {
        assertNull(productCatalogPolicy.normalizeOptionalText("   "));
    }

    @Test
    void shouldRejectDuplicateNameOwnedByAnotherProduct() {
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> productCatalogPolicy.ensureUniqueName(88L, 99L)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
    }
}
