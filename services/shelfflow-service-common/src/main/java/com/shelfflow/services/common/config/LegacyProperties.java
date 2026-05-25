package com.shelfflow.services.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@ConfigurationProperties(prefix = "shelfflow.legacy")
public class LegacyProperties {
    @NotBlank
    private String baseUrl;
}
