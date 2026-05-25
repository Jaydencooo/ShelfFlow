package com.shelfflow.services.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@ConfigurationProperties(prefix = "shelfflow.jwt")
public class JwtProperties {
    @NotBlank
    private String secret;

    @NotBlank
    private String expiresIn;
}
