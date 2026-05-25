package com.shelfflow.services.common.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.config.JwtProperties;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class UserAccessTokenService {

    private static final String AUTHORIZATION_PREFIX_PATTERN = "(?i)^Bearer\\s+";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String TOKEN_TYPE = "JWT";
    private static final String ALGORITHM = "HS256";
    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final JwtProperties jwtProperties;

    public UserAccessTokenService(ObjectMapper objectMapper, JwtProperties jwtProperties) {
        this.objectMapper = objectMapper;
        this.jwtProperties = jwtProperties;
    }

    public String issueToken(Long userId, String openId) {
        if (userId == null || openId == null || openId.isBlank()) {
            throw new ApplicationException(ErrorCode.INTERNAL_ERROR, "无法签发用户访问令牌");
        }
        try {
            String header = base64Url(objectMapper.writeValueAsBytes(Map.of("alg", ALGORITHM, "typ", TOKEN_TYPE)));
            Instant expiresAt = Instant.now().plus(Duration.parse(jwtProperties.getExpiresIn()));
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("userId", userId);
            claims.put("openId", openId);
            claims.put("exp", expiresAt.getEpochSecond());
            String payload = base64Url(objectMapper.writeValueAsBytes(claims));
            String signature = sign(header + "." + payload);
            return header + "." + payload + "." + signature;
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApplicationException(ErrorCode.INTERNAL_ERROR, "签发用户访问令牌失败");
        }
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
        return parseClaims(token).userId();
    }

    public String extractOpenIdFromToken(String token) {
        return parseClaims(token).openId();
    }

    private UserTokenClaims parseClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("token format invalid");
            }
            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = sign(signingInput);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("token signature invalid");
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payload, CLAIMS_TYPE);
            Object userIdClaim = claims.get("userId");
            Object openIdClaim = claims.get("openId");
            Object expClaim = claims.get("exp");
            if (userIdClaim == null || openIdClaim == null || expClaim == null) {
                throw new IllegalArgumentException("token claims missing");
            }

            long expiresAtEpoch = Long.parseLong(String.valueOf(expClaim));
            if (Instant.now().isAfter(Instant.ofEpochSecond(expiresAtEpoch))) {
                throw new IllegalArgumentException("token expired");
            }
            Long userId = Long.valueOf(String.valueOf(userIdClaim));
            String openId = String.valueOf(openIdClaim).trim();
            if (openId.isBlank()) {
                throw new IllegalArgumentException("openId missing");
            }
            return new UserTokenClaims(userId, openId);
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED, "无效的访问令牌");
        }
    }

    private String sign(String signingInput) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM));
        return base64Url(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private record UserTokenClaims(Long userId, String openId) {
    }
}
