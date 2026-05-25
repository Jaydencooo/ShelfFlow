package com.shelfflow.services.common.web;

import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Object>> handleApplicationException(ApplicationException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.error(exception.getErrorCode(), resolveRequestId(request), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("请求参数不合法");
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.status())
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, resolveRequestId(request), message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("请求参数不合法");
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.status())
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, resolveRequestId(request), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception, requestId={}, method={}, path={}",
                resolveRequestId(request),
                request.getMethod(),
                request.getRequestURI(),
                exception);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.status())
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, resolveRequestId(request), "服务内部错误"));
    }

    private String resolveRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestContext.REQUEST_ID_ATTRIBUTE);
        if (requestId instanceof String && !((String) requestId).isBlank()) {
            return (String) requestId;
        }
        String headerRequestId = request.getHeader(RequestContext.REQUEST_ID_HEADER);
        return headerRequestId == null || headerRequestId.isBlank() ? "unknown" : headerRequestId;
    }
}
