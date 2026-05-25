package com.shelfflow.services.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.config.JwtProperties;
import com.shelfflow.services.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserAccessTokenServiceTest {

    @Test
    void shouldIssueAndParseToken() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret");
        jwtProperties.setExpiresIn("PT12H");
        UserAccessTokenService service = new UserAccessTokenService(new ObjectMapper(), jwtProperties);

        String token = service.issueToken(9L, "openid-9");

        assertEquals(9L, service.extractUserIdFromToken(token));
        assertEquals("openid-9", service.extractOpenIdFromToken(token));
    }

    @Test
    void shouldRejectBrokenToken() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret");
        jwtProperties.setExpiresIn("PT12H");
        UserAccessTokenService service = new UserAccessTokenService(new ObjectMapper(), jwtProperties);

        ApplicationException exception = assertThrows(ApplicationException.class, () -> service.extractUserIdFromToken("broken-token"));

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }
}
