package com.shelfflow.services.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserOrderStatus {
    PENDING_PAYMENT("pending_payment", 1),
    TO_PREPARE("to_prepare", 2),
    PREPARING("preparing", 3),
    READY_FOR_PICKUP("ready_for_pickup", 4),
    COMPLETED("completed", 5),
    CANCELLED("cancelled", 6),
    REFUNDED("refunded", 7);

    private final String value;
    private final int legacyValue;

    UserOrderStatus(String value, int legacyValue) {
        this.value = value;
        this.legacyValue = legacyValue;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public int legacyValue() {
        return legacyValue;
    }

    public static UserOrderStatus fromLegacy(Integer legacyValue) {
        if (legacyValue == null) {
            return PENDING_PAYMENT;
        }
        for (UserOrderStatus status : values()) {
            if (status.legacyValue == legacyValue) {
                return status;
            }
        }
        return PENDING_PAYMENT;
    }

    @JsonCreator
    public static UserOrderStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return PENDING_PAYMENT;
        }
        for (UserOrderStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported order status: " + value);
    }
}
