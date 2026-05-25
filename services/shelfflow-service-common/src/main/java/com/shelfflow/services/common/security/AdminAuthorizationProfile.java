package com.shelfflow.services.common.security;

import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class AdminAuthorizationProfile {

    private static final Set<AdminRole> DEFAULT_ROLES = EnumSet.of(AdminRole.ADMIN);
    private static final Set<AdminPermission> DEFAULT_PERMISSIONS = EnumSet.of(
            AdminPermission.PRODUCT_READ,
            AdminPermission.PRODUCT_WRITE,
            AdminPermission.INVENTORY_READ,
            AdminPermission.INVENTORY_WRITE,
            AdminPermission.PRICING_READ,
            AdminPermission.PRICING_WRITE,
            AdminPermission.LOSS_STATS_READ,
            AdminPermission.AI_OPS_READ,
            AdminPermission.AI_OPS_WRITE,
            AdminPermission.ORDER_READ,
            AdminPermission.ORDER_WRITE
    );

    public Set<AdminRole> defaultRoles() {
        return EnumSet.copyOf(DEFAULT_ROLES);
    }

    public Set<AdminPermission> defaultPermissions() {
        return EnumSet.copyOf(DEFAULT_PERMISSIONS);
    }

    public List<String> defaultRoleValues() {
        return DEFAULT_ROLES.stream().map(AdminRole::value).toList();
    }

    public List<String> defaultPermissionValues() {
        return DEFAULT_PERMISSIONS.stream().map(AdminPermission::value).toList();
    }
}
