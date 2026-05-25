package com.shelfflow.services.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PricingStatus {
    PENDING("pending"),
    ACTIVE("active"),
    STALE("stale"),
    DISABLED("disabled");

    private final String value;

    PricingStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static PricingStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        for (PricingStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported pricing status: " + value);
    }
}
