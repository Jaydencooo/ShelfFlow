package com.shelfflow.services.auth.controller;

import com.shelfflow.services.auth.client.LegacyStaffClient;
import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.dto.AdminLoginRequest;
import com.shelfflow.services.common.dto.AdminSessionResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.common.legacy.LegacyEnvelope;
import com.shelfflow.services.common.security.AdminAccessTokenParser;
import com.shelfflow.services.common.security.AdminAuthorizationProfile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String LEGACY_LOGIN_FAILURE_MESSAGE = "legacy login failed";
    private static final String LEGACY_ME_FAILURE_MESSAGE = "legacy me failed";
    private static final String LEGACY_LOGIN_INVALID_SESSION_MESSAGE = "legacy login returned invalid session";
    private static final String LEGACY_ME_INVALID_PROFILE_MESSAGE = "legacy me returned invalid profile";

    private final LegacyStaffClient legacyStaffClient;
    private final AdminAccessTokenParser adminAccessTokenParser;
    private final AdminAuthorizationProfile adminAuthorizationProfile;

    public AdminAuthController(LegacyStaffClient legacyStaffClient,
                               AdminAccessTokenParser adminAccessTokenParser,
                               AdminAuthorizationProfile adminAuthorizationProfile) {
        this.legacyStaffClient = legacyStaffClient;
        this.adminAccessTokenParser = adminAccessTokenParser;
        this.adminAuthorizationProfile = adminAuthorizationProfile;
    }

    @PostMapping("/login")
    public ApiResponse<AdminSessionResponse> login(@Valid @RequestBody AdminLoginRequest request, HttpServletRequest servletRequest) {
        LegacyEnvelope<LegacyStaffClient.LegacyStaffLoginResponse> response = legacyStaffClient.login(request);
        LegacyStaffClient.LegacyStaffLoginResponse session = requireLegacySuccess(
                response,
                LEGACY_LOGIN_FAILURE_MESSAGE
        );
        validateLoginSession(session);
        return ApiResponse.success(
                AdminSessionResponse.builder()
                        .userId(String.valueOf(session.getId()))
                        .username(session.getUserName())
                        .displayName(session.getName())
                        .roles(adminAuthorizationProfile.defaultRoleValues())
                        .permissions(adminAuthorizationProfile.defaultPermissionValues())
                        .token(session.getToken())
                        .build(),
                resolveRequestId(servletRequest),
                "登录成功"
        );
    }

    @GetMapping("/me")
    public ApiResponse<AdminSessionResponse> me(@RequestHeader("Authorization") String authorization, HttpServletRequest servletRequest) {
        String token = adminAccessTokenParser.extractBearerToken(authorization);
        Long staffId = adminAccessTokenParser.extractUserIdFromToken(token);
        LegacyEnvelope<LegacyStaffClient.LegacyStaffResponse> response = legacyStaffClient.getById(staffId, token);
        LegacyStaffClient.LegacyStaffResponse staff = requireLegacySuccess(
                response,
                LEGACY_ME_FAILURE_MESSAGE
        );
        validateStaffProfile(staff);
        return ApiResponse.success(
                AdminSessionResponse.builder()
                        .userId(String.valueOf(staff.getId()))
                        .username(staff.getUsername())
                        .displayName(staff.getName())
                        .roles(adminAuthorizationProfile.defaultRoleValues())
                        .permissions(adminAuthorizationProfile.defaultPermissionValues())
                        .token(token)
                        .build(),
                resolveRequestId(servletRequest),
                "查询成功"
        );
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }

    private <T> T requireLegacySuccess(LegacyEnvelope<T> response, String fallbackMessage) {
        if (response == null || response.getCode() == null || response.getCode() != 1 || response.getData() == null) {
            throw new ApplicationException(
                    ErrorCode.DEPENDENCY_ERROR,
                    response == null || response.getMsg() == null || response.getMsg().isBlank()
                            ? fallbackMessage
                            : response.getMsg()
            );
        }
        return response.getData();
    }

    private void validateLoginSession(LegacyStaffClient.LegacyStaffLoginResponse session) {
        if (session.getId() == null || isBlank(session.getUserName()) || isBlank(session.getToken())) {
            throw new ApplicationException(ErrorCode.DEPENDENCY_ERROR, LEGACY_LOGIN_INVALID_SESSION_MESSAGE);
        }
    }

    private void validateStaffProfile(LegacyStaffClient.LegacyStaffResponse staff) {
        if (staff.getId() == null || isBlank(staff.getUsername())) {
            throw new ApplicationException(ErrorCode.DEPENDENCY_ERROR, LEGACY_ME_INVALID_PROFILE_MESSAGE);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
