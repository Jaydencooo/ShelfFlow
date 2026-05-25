package com.shelfflow.services.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductStatus {
    INACTIVE("inactive", 0),
    ACTIVE("active", 1);

    private final String value;
    private final int legacyValue;

    ProductStatus(String value, int legacyValue) {
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

    public static ProductStatus fromLegacy(Integer legacyValue) {
        return legacyValue != null && legacyValue == 1 ? ACTIVE : INACTIVE;
    }

    @JsonCreator
    public static ProductStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE;
        }
        for (ProductStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported product status: " + value);
    }
}
