package com.shelfflow.services.user.controller;

import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.dto.UserLoginRequest;
import com.shelfflow.services.common.dto.UserPasswordResetRequest;
import com.shelfflow.services.common.dto.UserProfileUpdateRequest;
import com.shelfflow.services.common.dto.UserRegisterRequest;
import com.shelfflow.services.common.dto.UserSessionResponse;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.auth.service.UserSessionApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/user/auth")
public class UserAuthController {

    private final UserSessionApplicationService userSessionApplicationService;

    public UserAuthController(UserSessionApplicationService userSessionApplicationService) {
        this.userSessionApplicationService = userSessionApplicationService;
    }

    @PostMapping("/register")
    public ApiResponse<UserSessionResponse> register(@Valid @RequestBody UserRegisterRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userSessionApplicationService.register(request),
                requestId(servletRequest),
                "注册成功"
        );
    }

    @PostMapping("/login")
    public ApiResponse<UserSessionResponse> login(@Valid @RequestBody UserLoginRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userSessionApplicationService.login(request),
                requestId(servletRequest),
                "登录成功"
        );
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody UserPasswordResetRequest request, HttpServletRequest servletRequest) {
        userSessionApplicationService.resetPassword(request);
        return ApiResponse.success(null, requestId(servletRequest), "密码重置成功");
    }

    @GetMapping("/me")
    public ApiResponse<UserSessionResponse> me(UserAuthenticatedUser authenticatedUser, HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userSessionApplicationService.currentSession(authenticatedUser),
                requestId(servletRequest),
                "查询成功"
        );
    }

    @PutMapping("/me")
    public ApiResponse<UserSessionResponse> updateProfile(UserAuthenticatedUser authenticatedUser,
                                                          @Valid @RequestBody UserProfileUpdateRequest request,
                                                          HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userSessionApplicationService.updateProfile(authenticatedUser, request),
                requestId(servletRequest),
                "资料更新成功"
        );
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
