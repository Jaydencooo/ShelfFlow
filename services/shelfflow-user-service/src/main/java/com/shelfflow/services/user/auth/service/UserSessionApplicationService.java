package com.shelfflow.services.user.auth.service;

import com.shelfflow.services.common.domain.UserAccountStatus;
import com.shelfflow.services.common.dto.UserLoginRequest;
import com.shelfflow.services.common.dto.UserPasswordChangeRequest;
import com.shelfflow.services.common.dto.UserPasswordResetRequest;
import com.shelfflow.services.common.dto.UserProfileUpdateRequest;
import com.shelfflow.services.common.dto.UserRegisterRequest;
import com.shelfflow.services.common.dto.UserSessionResponse;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.common.security.UserAccessTokenService;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.auth.domain.UserAccountPolicy;
import com.shelfflow.services.user.auth.persistence.UserAccountPersistenceMapper;
import com.shelfflow.services.user.auth.persistence.dataobject.UserAccountDataObject;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserSessionApplicationService {

    private final UserAccountPersistenceMapper userAccountPersistenceMapper;
    private final UserAccountPolicy userAccountPolicy;
    private final UserAccessTokenService userAccessTokenService;
    private final UserVerificationCodeApplicationService userVerificationCodeApplicationService;
    private final PasswordEncoder passwordEncoder;

    public UserSessionApplicationService(UserAccountPersistenceMapper userAccountPersistenceMapper,
                                         UserAccountPolicy userAccountPolicy,
                                         UserAccessTokenService userAccessTokenService,
                                         UserVerificationCodeApplicationService userVerificationCodeApplicationService,
                                         PasswordEncoder passwordEncoder) {
        this.userAccountPersistenceMapper = userAccountPersistenceMapper;
        this.userAccountPolicy = userAccountPolicy;
        this.userAccessTokenService = userAccessTokenService;
        this.userVerificationCodeApplicationService = userVerificationCodeApplicationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserSessionResponse register(UserRegisterRequest request) {
        UserAccountPolicy.RegistrationAccount registrationAccount = userAccountPolicy.normalizeRegistrationAccount(
                request.getAccount(),
                request.getOpenId(),
                request.getPhone(),
                request.getEmail()
        );
        String name = userAccountPolicy.normalizeRequiredName(request.getName());
        String password = userAccountPolicy.normalizePassword(request.getPassword(), "密码");
        String confirmPassword = userAccountPolicy.normalizePassword(request.getConfirmPassword(), "确认密码");
        userAccountPolicy.ensurePasswordConfirmed(password, confirmPassword);

        UserAccountDataObject existingUser = userAccountPersistenceMapper.findByLoginAccount(registrationAccount.account());
        userAccountPolicy.ensureRegisterAvailable(existingUser);
        if (registrationAccount.phone() != null) {
            userAccountPolicy.ensurePhoneAvailable(userAccountPersistenceMapper.findByPhone(registrationAccount.phone()));
        }
        if (registrationAccount.email() != null) {
            userAccountPolicy.ensureEmailAvailable(userAccountPersistenceMapper.findByEmail(registrationAccount.email()));
        }
        userVerificationCodeApplicationService.verifyAndConsume(
                registrationAccount.account(),
                UserAccountPolicy.VERIFICATION_PURPOSE_REGISTER,
                request.getVerificationCode()
        );

        LocalDateTime now = LocalDateTime.now();
        UserAccountDataObject user = new UserAccountDataObject();
        user.setOpenId(registrationAccount.account());
        user.setName(name);
        user.setPhone(registrationAccount.phone());
        user.setEmail(registrationAccount.email());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(UserAccountStatus.ACTIVE.code());
        user.setCreateTime(now);
        user.setUpdateTime(now);
        try {
            userAccountPersistenceMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new ApplicationException(ErrorCode.CONFLICT, "账号已存在，请直接登录或找回密码");
        }

        return toSessionResponse(user, userAccessTokenService.issueToken(user.getId(), user.getOpenId()));
    }

    @Transactional
    public UserSessionResponse login(UserLoginRequest request) {
        String account = userAccountPolicy.normalizeLoginAccount(request.getAccount(), request.getOpenId());
        String password = userAccountPolicy.normalizePassword(request.getPassword(), "密码");

        UserAccountDataObject user = userAccountPersistenceMapper.findByLoginAccount(account);
        userAccountPolicy.ensureLoginAllowed(user);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }

        return toSessionResponse(user, userAccessTokenService.issueToken(user.getId(), user.getOpenId()));
    }

    @Transactional
    public void resetPassword(UserPasswordResetRequest request) {
        String account = userAccountPolicy.normalizeLoginAccount(request.getAccount(), firstNonBlank(request.getOpenId(), request.getPhone()));
        String newPassword = userAccountPolicy.normalizePassword(request.getNewPassword(), "新密码");
        String confirmPassword = userAccountPolicy.normalizePassword(request.getConfirmPassword(), "确认密码");
        userAccountPolicy.ensurePasswordConfirmed(newPassword, confirmPassword);

        UserAccountDataObject user = userAccountPersistenceMapper.findByLoginAccount(account);
        userAccountPolicy.ensureResetTargetExists(user);
        userVerificationCodeApplicationService.verifyAndConsume(
                account,
                UserAccountPolicy.VERIFICATION_PURPOSE_RESET_PASSWORD,
                request.getVerificationCode()
        );

        UserAccountDataObject update = new UserAccountDataObject();
        update.setId(user.getId());
        update.setPasswordHash(passwordEncoder.encode(newPassword));
        update.setUpdateTime(LocalDateTime.now());
        userAccountPersistenceMapper.updatePasswordById(update);
    }

    public UserSessionResponse currentSession(UserAuthenticatedUser authenticatedUser) {
        UserAccountDataObject user = userAccountPersistenceMapper.findById(authenticatedUser.getUserId());
        userAccountPolicy.ensureUserExists(user != null);
        userAccountPolicy.ensureLoginAllowed(user);
        return toSessionResponse(user, authenticatedUser.getAccessToken());
    }

    @Transactional
    public UserSessionResponse updateProfile(UserAuthenticatedUser authenticatedUser, UserProfileUpdateRequest request) {
        UserAccountDataObject user = userAccountPersistenceMapper.findById(authenticatedUser.getUserId());
        userAccountPolicy.ensureUserExists(user != null);
        userAccountPolicy.ensureLoginAllowed(user);

        UserAccountDataObject update = new UserAccountDataObject();
        update.setId(authenticatedUser.getUserId());
        update.setName(userAccountPolicy.normalizeRequiredName(request.getName()));
        String normalizedPhone = userAccountPolicy.normalizeOptionalPhone(request.getPhone());
        String normalizedEmail = userAccountPolicy.normalizeOptionalEmail(request.getEmail());
        verifySensitiveProfileChange(user, normalizedPhone, normalizedEmail, request);
        update.setPhone(normalizedPhone);
        update.setEmail(normalizedEmail);
        update.setUpdateTime(LocalDateTime.now());

        int affectedRows = userAccountPersistenceMapper.updateProfile(update);
        if (affectedRows <= 0) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "用户账号不存在");
        }

        UserAccountDataObject updated = userAccountPersistenceMapper.findById(authenticatedUser.getUserId());
        return toSessionResponse(updated, authenticatedUser.getAccessToken());
    }

    @Transactional
    public void changePassword(UserAuthenticatedUser authenticatedUser, UserPasswordChangeRequest request) {
        UserAccountDataObject user = userAccountPersistenceMapper.findById(authenticatedUser.getUserId());
        userAccountPolicy.ensureUserExists(user != null);
        userAccountPolicy.ensureLoginAllowed(user);

        String currentPassword = userAccountPolicy.normalizePassword(request.getCurrentPassword(), "当前密码");
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED, "当前密码不正确");
        }

        String newPassword = userAccountPolicy.normalizePassword(request.getNewPassword(), "新密码");
        String confirmPassword = userAccountPolicy.normalizePassword(request.getConfirmPassword(), "确认密码");
        userAccountPolicy.ensurePasswordConfirmed(newPassword, confirmPassword);
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "新密码不能与当前密码相同");
        }

        UserAccountDataObject update = new UserAccountDataObject();
        update.setId(user.getId());
        update.setPasswordHash(passwordEncoder.encode(newPassword));
        update.setUpdateTime(LocalDateTime.now());
        userAccountPersistenceMapper.updatePasswordById(update);
    }

    private void verifySensitiveProfileChange(UserAccountDataObject user,
                                              String normalizedPhone,
                                              String normalizedEmail,
                                              UserProfileUpdateRequest request) {
        if (isChanged(user.getPhone(), normalizedPhone)) {
            if (normalizedPhone == null) {
                throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "手机号不能为空");
            }
            userAccountPolicy.ensurePhoneAvailableForCurrentUser(
                    userAccountPersistenceMapper.findByPhone(normalizedPhone),
                    user.getId()
            );
            userVerificationCodeApplicationService.verifyAndConsume(
                    normalizedPhone,
                    UserAccountPolicy.VERIFICATION_PURPOSE_CHANGE_PHONE,
                    request.getPhoneVerificationCode()
            );
        }
        if (isChanged(user.getEmail(), normalizedEmail)) {
            if (normalizedEmail == null) {
                throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "邮箱不能为空");
            }
            userAccountPolicy.ensureEmailAvailableForCurrentUser(
                    userAccountPersistenceMapper.findByEmail(normalizedEmail),
                    user.getId()
            );
            userVerificationCodeApplicationService.verifyAndConsume(
                    normalizedEmail,
                    UserAccountPolicy.VERIFICATION_PURPOSE_CHANGE_EMAIL,
                    request.getEmailVerificationCode()
            );
        }
    }

    private UserSessionResponse toSessionResponse(UserAccountDataObject user, String token) {
        return UserSessionResponse.builder()
                .userId(String.valueOf(user.getId()))
                .openId(user.getOpenId())
                .account(user.getOpenId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .token(token)
                .build();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private boolean isChanged(String currentValue, String nextValue) {
        String current = currentValue == null || currentValue.isBlank() ? null : currentValue.trim();
        String next = nextValue == null || nextValue.isBlank() ? null : nextValue.trim();
        if (current == null) {
            return next != null;
        }
        return !current.equals(next);
    }
}
