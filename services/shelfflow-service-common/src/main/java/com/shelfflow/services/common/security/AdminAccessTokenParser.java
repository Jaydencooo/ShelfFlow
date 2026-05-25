package com.shelfflow.services.common.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class AdminAccessTokenParser {

    private static final String AUTHORIZATION_PREFIX_PATTERN = "(?i)^Bearer\\s+";

    private final ObjectMapper objectMapper;

    public AdminAccessTokenParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED, "缺少访问令牌");
        }

        String token = authorization.replaceFirst(AUTHORIZATION_PREFIX_PATTERN, "").trim();
        if (token.isBlank()) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED, "缺少访问令牌");
        }
        return token;
    }

    public Long extractUserId(String authorization) {
        return extractUserIdFromToken(extractBearerToken(authorization));
    }

    public Long extractUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("token payload missing");
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
            Object empId = claims.get("empId");
            if (empId == null) {
                throw new IllegalArgumentException("empId missing");
            }
            return Long.valueOf(String.valueOf(empId));
        } catch (Exception exception) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED, "无效的访问令牌");
        }
    }
}
