package com.shelfflow.services.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "shelfflow.runtime")
public class RuntimeProperties {
    @NotBlank
    private String appName;

    @NotBlank
    private String appEnv;

    @NotBlank
    private String logLevel;

    @NotBlank
    private String apiPrefix;

    private List<String> corsAllowedOrigins = new ArrayList<>();
}
