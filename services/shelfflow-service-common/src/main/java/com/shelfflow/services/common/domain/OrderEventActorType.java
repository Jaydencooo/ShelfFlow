package com.shelfflow.services.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderEventActorType {
    USER("user"),
    ADMIN("admin"),
    SYSTEM("system");

    private final String value;

    OrderEventActorType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static OrderEventActorType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return SYSTEM;
        }
        for (OrderEventActorType actorType : values()) {
            if (actorType.value.equalsIgnoreCase(value)) {
                return actorType;
            }
        }
        return SYSTEM;
    }
}
