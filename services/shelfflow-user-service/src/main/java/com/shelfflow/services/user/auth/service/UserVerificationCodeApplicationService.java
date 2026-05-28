package com.shelfflow.services.user.auth.service;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.dto.UserVerificationCodeRequest;
import com.shelfflow.services.common.dto.UserVerificationCodeResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.user.auth.domain.UserAccountPolicy;
import com.shelfflow.services.user.auth.persistence.UserVerificationCodePersistenceMapper;
import com.shelfflow.services.user.auth.persistence.dataobject.UserVerificationCodeDataObject;
import com.shelfflow.services.user.config.UserAuthProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class UserVerificationCodeApplicationService {

    private static final int DECIMAL_BOUND = 10;

    private final UserVerificationCodePersistenceMapper verificationCodePersistenceMapper;
    private final UserAccountPolicy userAccountPolicy;
    private final UserAuthProperties userAuthProperties;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeDeliveryService verificationCodeDeliveryService;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserVerificationCodeApplicationService(UserVerificationCodePersistenceMapper verificationCodePersistenceMapper,
                                                  UserAccountPolicy userAccountPolicy,
                                                  UserAuthProperties userAuthProperties,
                                                  PasswordEncoder passwordEncoder,
                                                  VerificationCodeDeliveryService verificationCodeDeliveryService) {
        this.verificationCodePersistenceMapper = verificationCodePersistenceMapper;
        this.userAccountPolicy = userAccountPolicy;
        this.userAuthProperties = userAuthProperties;
        this.passwordEncoder = passwordEncoder;
        this.verificationCodeDeliveryService = verificationCodeDeliveryService;
    }

    @Transactional
    public UserVerificationCodeResponse send(UserVerificationCodeRequest request) {
        String account = userAccountPolicy.normalizeLoginAccount(request.getAccount(), null);
        String purpose = userAccountPolicy.normalizeVerificationPurpose(request.getPurpose());
        String code = generateNumericCode(userAuthProperties.getVerificationCodeLength());
        LocalDateTime now = LocalDateTime.now();

        UserVerificationCodeDataObject verificationCode = new UserVerificationCodeDataObject();
        verificationCode.setTarget(account);
        verificationCode.setPurpose(purpose);
        verificationCode.setCodeHash(passwordEncoder.encode(code));
        verificationCode.setExpiresAt(now.plusSeconds(userAuthProperties.getVerificationCodeTtlSeconds()));
        verificationCode.setCreateTime(now);
        verificationCodePersistenceMapper.insert(verificationCode);
        verificationCodeDeliveryService.deliver(account, purpose, code, userAuthProperties.getVerificationCodeTtlSeconds());

        return UserVerificationCodeResponse.builder()
                .target(account)
                .purpose(purpose)
                .expiresInSeconds(userAuthProperties.getVerificationCodeTtlSeconds())
                .debugCode(userAuthProperties.isExposeDebugVerificationCode() ? code : null)
                .build();
    }

    @Transactional
    public void verifyAndConsume(String account, String purpose, String rawCode) {
        String normalizedCode = userAccountPolicy.normalizeVerificationCode(rawCode);
        LocalDateTime now = LocalDateTime.now();
        UserVerificationCodeDataObject verificationCode = verificationCodePersistenceMapper.findLatestAvailable(account, purpose, now);
        if (verificationCode == null || !passwordEncoder.matches(normalizedCode, verificationCode.getCodeHash())) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "验证码错误或已过期");
        }

        int affectedRows = verificationCodePersistenceMapper.markConsumed(verificationCode.getId(), now);
        if (affectedRows <= 0) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "验证码已被使用，请重新获取");
        }
    }

    private String generateNumericCode(int length) {
        if (length < 4 || length > 10) {
            throw new ApplicationException(ErrorCode.INTERNAL_ERROR, "验证码长度配置不合法");
        }
        StringBuilder code = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            code.append(secureRandom.nextInt(DECIMAL_BOUND));
        }
        return code.toString();
    }
}
