package com.shelfflow.services.user.auth.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVerificationCodeDataObject {
    private Long id;
    private String target;
    private String purpose;
    private String codeHash;
    private LocalDateTime expiresAt;
    private LocalDateTime consumedAt;
    private LocalDateTime createTime;
}
