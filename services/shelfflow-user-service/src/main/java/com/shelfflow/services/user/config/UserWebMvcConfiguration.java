package com.shelfflow.services.user.config;

import com.shelfflow.services.common.security.UserAccessTokenService;
import com.shelfflow.services.common.security.UserAuthenticatedUserArgumentResolver;
import com.shelfflow.services.common.security.UserAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class UserWebMvcConfiguration implements WebMvcConfigurer {

    private static final String USER_CART_API_PATTERN = "/api/user/cart/**";
    private static final String USER_ME_API_PATTERN = "/api/user/auth/me";
    private static final String USER_ORDER_API_PATTERN = "/api/user/orders/**";
    private static final String USER_PICKUP_CONTACT_API_PATTERN = "/api/user/pickup-contacts/**";

    private final UserAccessTokenService userAccessTokenService;

    public UserWebMvcConfiguration(UserAccessTokenService userAccessTokenService) {
        this.userAccessTokenService = userAccessTokenService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        UserAuthenticationInterceptor interceptor = new UserAuthenticationInterceptor(userAccessTokenService);
        registry.addInterceptor(interceptor).addPathPatterns(
                USER_CART_API_PATTERN,
                USER_ME_API_PATTERN,
                USER_ORDER_API_PATTERN,
                USER_PICKUP_CONTACT_API_PATTERN
        );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserAuthenticatedUserArgumentResolver());
    }
}
