package com.shelfflow.services.admin.config;

import com.shelfflow.services.common.security.AdminAccessTokenParser;
import com.shelfflow.services.common.security.AdminAuthorizationProfile;
import com.shelfflow.services.common.security.AdminAuthenticatedUserArgumentResolver;
import com.shelfflow.services.common.security.AdminAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class AdminWebMvcConfiguration implements WebMvcConfigurer {

    private static final String ADMIN_API_PATH_PATTERN = "/api/admin/**";
    private static final String ADMIN_AUTH_API_PATH_PATTERN = "/api/admin/auth/**";

    private final AdminAccessTokenParser adminAccessTokenParser;
    private final AdminAuthorizationProfile adminAuthorizationProfile;

    public AdminWebMvcConfiguration(AdminAccessTokenParser adminAccessTokenParser,
                                   AdminAuthorizationProfile adminAuthorizationProfile) {
        this.adminAccessTokenParser = adminAccessTokenParser;
        this.adminAuthorizationProfile = adminAuthorizationProfile;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthenticationInterceptor(adminAccessTokenParser))
                .addPathPatterns(ADMIN_API_PATH_PATTERN)
                .excludePathPatterns(ADMIN_AUTH_API_PATH_PATTERN);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AdminAuthenticatedUserArgumentResolver(adminAuthorizationProfile));
    }
}
