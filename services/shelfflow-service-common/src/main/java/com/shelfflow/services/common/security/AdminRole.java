package com.shelfflow.services.common.security;

public enum AdminRole {
    ADMIN("admin");

    private final String value;

    AdminRole(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
