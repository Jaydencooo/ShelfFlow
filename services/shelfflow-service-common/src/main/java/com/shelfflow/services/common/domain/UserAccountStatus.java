package com.shelfflow.services.common.domain;

public enum UserAccountStatus {
    DISABLED(0),
    ACTIVE(1);

    private final int code;

    UserAccountStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static UserAccountStatus fromCode(Integer code) {
        if (code == null) {
            return ACTIVE;
        }
        for (UserAccountStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return ACTIVE;
    }
}
