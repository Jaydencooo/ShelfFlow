package com.shelfflow.services.common.security;

public enum AdminPermission {
    PRODUCT_READ("product:read"),
    PRODUCT_WRITE("product:write"),
    INVENTORY_READ("inventory:read"),
    INVENTORY_WRITE("inventory:write"),
    PRICING_READ("pricing:read"),
    PRICING_WRITE("pricing:write"),
    LOSS_STATS_READ("loss-stats:read"),
    AI_OPS_READ("ai-ops:read"),
    AI_OPS_WRITE("ai-ops:write"),
    ORDER_READ("order:read"),
    ORDER_WRITE("order:write"),
    OPERATION_LOG_READ("operation-log:read");

    private final String value;

    AdminPermission(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
