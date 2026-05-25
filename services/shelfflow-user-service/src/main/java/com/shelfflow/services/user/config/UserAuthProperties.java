package com.shelfflow.services.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "shelfflow.user.auth")
public class UserAuthProperties {

    private int minAccountLength = 4;
    private int maxAccountLength = 64;
    private int minPasswordLength = 8;
    private int maxPasswordLength = 32;
    private int minDisplayNameLength = 2;
    private int maxDisplayNameLength = 32;
}
