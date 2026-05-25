package com.shelfflow.services.common.api;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder(toBuilder = true)
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    private String requestId;
    private OffsetDateTime timestamp;

    public static <T> ApiResponse<T> success(T data, String requestId, String message) {
        return ApiResponse.<T>builder()
                .code("ok")
                .message(message)
                .data(data)
                .requestId(requestId)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static ApiResponse<Object> error(ErrorCode errorCode, String requestId, String message) {
        return ApiResponse.builder()
                .code(errorCode.code())
                .message(message)
                .data(null)
                .requestId(requestId)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
