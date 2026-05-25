package com.shelfflow.services.common.security;

import com.shelfflow.services.common.web.RequestContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserAuthenticationInterceptor implements HandlerInterceptor {

    private final UserAccessTokenService userAccessTokenService;

    public UserAuthenticationInterceptor(UserAccessTokenService userAccessTokenService) {
        this.userAccessTokenService = userAccessTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        String accessToken = userAccessTokenService.extractBearerToken(authorization);
        Long userId = userAccessTokenService.extractUserIdFromToken(accessToken);
        String openId = userAccessTokenService.extractOpenIdFromToken(accessToken);
        request.setAttribute(RequestContext.USER_ACCESS_TOKEN_ATTRIBUTE, accessToken);
        request.setAttribute(RequestContext.USER_ID_ATTRIBUTE, userId);
        request.setAttribute(RequestContext.USER_OPEN_ID_ATTRIBUTE, openId);
        return true;
    }
}
