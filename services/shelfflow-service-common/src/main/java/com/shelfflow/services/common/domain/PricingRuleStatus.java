package com.shelfflow.services.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PricingRuleStatus {
    DISABLED(0, "disabled"),
    ENABLED(1, "enabled");

    private final int legacyValue;
    private final String value;

    PricingRuleStatus(int legacyValue, String value) {
        this.legacyValue = legacyValue;
        this.value = value;
    }

    public int legacyValue() {
        return legacyValue;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public static PricingRuleStatus fromLegacy(Integer legacyValue) {
        if (legacyValue == null) {
            return DISABLED;
        }
        for (PricingRuleStatus status : values()) {
            if (status.legacyValue == legacyValue) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported pricing rule status: " + legacyValue);
    }

    @JsonCreator
    public static PricingRuleStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return ENABLED;
        }
        for (PricingRuleStatus status : values()) {
            if (status.value.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported pricing rule status: " + value);
    }
}
