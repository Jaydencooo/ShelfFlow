package com.shelfflow.services.common.domain;

public enum UserOrderPaymentStatus {
    PENDING(0),
    SUCCEEDED(1),
    FAILED(2);

    private final int legacyValue;

    UserOrderPaymentStatus(int legacyValue) {
        this.legacyValue = legacyValue;
    }

    public int legacyValue() {
        return legacyValue;
    }

    public static UserOrderPaymentStatus fromLegacy(Integer legacyValue) {
        if (legacyValue == null) {
            return PENDING;
        }
        for (UserOrderPaymentStatus status : values()) {
            if (status.legacyValue == legacyValue) {
                return status;
            }
        }
        return PENDING;
    }
}
