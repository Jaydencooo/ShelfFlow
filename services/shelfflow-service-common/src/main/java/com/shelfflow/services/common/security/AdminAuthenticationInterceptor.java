package com.shelfflow.services.common.security;

import com.shelfflow.services.common.web.RequestContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminAuthenticationInterceptor implements HandlerInterceptor {

    private final AdminAccessTokenParser adminAccessTokenParser;

    public AdminAuthenticationInterceptor(AdminAccessTokenParser adminAccessTokenParser) {
        this.adminAccessTokenParser = adminAccessTokenParser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        String accessToken = adminAccessTokenParser.extractBearerToken(authorization);
        Long userId = adminAccessTokenParser.extractUserIdFromToken(accessToken);
        request.setAttribute(RequestContext.ADMIN_ACCESS_TOKEN_ATTRIBUTE, accessToken);
        request.setAttribute(RequestContext.ADMIN_USER_ID_ATTRIBUTE, userId);
        return true;
    }
}
