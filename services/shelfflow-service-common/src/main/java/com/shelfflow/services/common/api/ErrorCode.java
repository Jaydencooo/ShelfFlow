package com.shelfflow.services.common.api;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR("validation_error", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", HttpStatus.FORBIDDEN),
    NOT_FOUND("not_found", HttpStatus.NOT_FOUND),
    CONFLICT("conflict", HttpStatus.CONFLICT),
    RATE_LIMITED("rate_limited", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR("internal_error", HttpStatus.INTERNAL_SERVER_ERROR),
    DEPENDENCY_ERROR("dependency_error", HttpStatus.BAD_GATEWAY);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}
