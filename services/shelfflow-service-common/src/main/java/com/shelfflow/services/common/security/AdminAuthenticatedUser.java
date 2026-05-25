package com.shelfflow.services.common.security;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;

import java.util.Collections;
import java.util.Set;

public class AdminAuthenticatedUser {

    private final Long userId;
    private final String accessToken;
    private final Set<AdminRole> roles;
    private final Set<AdminPermission> permissions;

    public AdminAuthenticatedUser(Long userId,
                                  String accessToken,
                                  Set<AdminRole> roles,
                                  Set<AdminPermission> permissions) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.roles = roles == null ? Collections.emptySet() : Set.copyOf(roles);
        this.permissions = permissions == null ? Collections.emptySet() : Set.copyOf(permissions);
    }

    public Long getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Set<AdminRole> getRoles() {
        return roles;
    }

    public Set<AdminPermission> getPermissions() {
        return permissions;
    }

    public void requirePermission(AdminPermission permission) {
        if (!permissions.contains(permission)) {
            throw new ApplicationException(ErrorCode.FORBIDDEN, "当前账号无权限执行该操作");
        }
    }
}
