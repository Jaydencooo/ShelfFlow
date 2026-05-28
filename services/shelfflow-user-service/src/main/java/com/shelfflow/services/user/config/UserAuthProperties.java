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
    private int verificationCodeLength = 6;
    private int verificationCodeTtlSeconds = 300;
    private boolean exposeDebugVerificationCode = true;
    private Mail mail = new Mail();

    @Data
    public static class Mail {
        private boolean enabled = false;
        private String from;
        private String fromName = "ShelfFlow";
        private String subject = "ShelfFlow 验证码";
    }
}
