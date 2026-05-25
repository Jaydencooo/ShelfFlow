package com.shelfflow.services.common.security;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminAuthenticatedUserTest {

    @Test
    void shouldRejectMissingPermission() {
        AdminAuthenticatedUser authenticatedUser = new AdminAuthenticatedUser(
                1L,
                "token",
                EnumSet.of(AdminRole.ADMIN),
                EnumSet.of(AdminPermission.PRODUCT_READ)
        );

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> authenticatedUser.requirePermission(AdminPermission.PRODUCT_WRITE)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }
}
