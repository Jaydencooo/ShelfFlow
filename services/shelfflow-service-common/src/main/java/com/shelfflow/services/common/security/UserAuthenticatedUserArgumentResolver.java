package com.shelfflow.services.common.security;

import com.shelfflow.services.common.web.RequestContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class UserAuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return UserAuthenticatedUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }
        Object userIdAttribute = request.getAttribute(RequestContext.USER_ID_ATTRIBUTE);
        Object openIdAttribute = request.getAttribute(RequestContext.USER_OPEN_ID_ATTRIBUTE);
        Object accessTokenAttribute = request.getAttribute(RequestContext.USER_ACCESS_TOKEN_ATTRIBUTE);
        if (!(userIdAttribute instanceof Long) || !(openIdAttribute instanceof String) || !(accessTokenAttribute instanceof String)) {
            return null;
        }
        return new UserAuthenticatedUser((Long) userIdAttribute, (String) openIdAttribute, (String) accessTokenAttribute);
    }
}
