package com.shelfflow.services.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BatchStatus {
    DRAFT("draft", 0),
    ACTIVE("active", 1),
    PAUSED("paused", 0),
    SOLD_OUT("sold_out", 2),
    EXPIRED("expired", 3);

    private final String value;
    private final int legacyValue;

    BatchStatus(String value, int legacyValue) {
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

    public static BatchStatus fromLegacy(Integer legacyValue) {
        if (legacyValue == null) {
            return DRAFT;
        }
        switch (legacyValue) {
            case 0:
                return PAUSED;
            case 2:
                return SOLD_OUT;
            case 3:
                return EXPIRED;
            default:
                return ACTIVE;
        }
    }

    @JsonCreator
    public static BatchStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return DRAFT;
        }
        for (BatchStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported batch status: " + value);
    }
}
