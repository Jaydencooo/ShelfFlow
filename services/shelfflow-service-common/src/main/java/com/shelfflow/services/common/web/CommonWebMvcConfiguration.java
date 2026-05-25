package com.shelfflow.services.common.web;

import com.shelfflow.services.common.config.RuntimeProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CommonWebMvcConfiguration implements WebMvcConfigurer {
    private final RuntimeProperties runtimeProperties;

    public CommonWebMvcConfiguration(RuntimeProperties runtimeProperties) {
        this.runtimeProperties = runtimeProperties;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(runtimeProperties.getCorsAllowedOrigins().toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
