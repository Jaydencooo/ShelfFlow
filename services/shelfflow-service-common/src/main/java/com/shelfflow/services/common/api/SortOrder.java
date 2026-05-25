package com.shelfflow.services.common.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SortOrder {
    ASC("asc"),
    DESC("desc");

    private final String value;

    SortOrder(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static SortOrder fromValue(String value) {
        if (value == null || value.isBlank()) {
            return DESC;
        }
        for (SortOrder order : values()) {
            if (order.value.equalsIgnoreCase(value)) {
                return order;
            }
        }
        throw new IllegalArgumentException("Unsupported sort order: " + value);
    }
}
