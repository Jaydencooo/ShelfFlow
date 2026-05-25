package com.shelfflow.services.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserOrderPayStatus {
    UNPAID("unpaid", 0),
    PAID("paid", 1),
    REFUNDED("refunded", 2);

    private final String value;
    private final int legacyValue;

    UserOrderPayStatus(String value, int legacyValue) {
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

    public static UserOrderPayStatus fromLegacy(Integer legacyValue) {
        if (legacyValue == null) {
            return UNPAID;
        }
        for (UserOrderPayStatus status : values()) {
            if (status.legacyValue == legacyValue) {
                return status;
            }
        }
        return UNPAID;
    }

    @JsonCreator
    public static UserOrderPayStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return UNPAID;
        }
        for (UserOrderPayStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported pay status: " + value);
    }
}
