package com.shelfflow.services.common.exception;

import com.shelfflow.services.common.api.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus status;

    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = errorCode.status();
    }

    public ApplicationException(String code, HttpStatus status, String message) {
        super(message);
        this.errorCode = resolveErrorCode(code);
        this.status = status;
    }

    public String getCode() {
        return errorCode.code();
    }

    private ErrorCode resolveErrorCode(String code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code().equals(code)) {
                return errorCode;
            }
        }
        return ErrorCode.INTERNAL_ERROR;
    }
}
