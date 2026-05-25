package com.shelfflow.services.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderEventType {
    SUBMITTED("submitted"),
    PAID("paid"),
    CANCELLED("cancelled"),
    FULFILLMENT_UPDATED("fulfillment_updated");

    private final String value;

    OrderEventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static OrderEventType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return FULFILLMENT_UPDATED;
        }
        for (OrderEventType eventType : values()) {
            if (eventType.value.equalsIgnoreCase(value)) {
                return eventType;
            }
        }
        return FULFILLMENT_UPDATED;
    }
}
