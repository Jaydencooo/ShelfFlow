package com.shelfflow.services.user.auth.service;

import com.shelfflow.services.common.domain.UserAccountStatus;
import com.shelfflow.services.common.dto.UserLoginRequest;
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
    private final PasswordEncoder passwordEncoder;

    public UserSessionApplicationService(UserAccountPersistenceMapper userAccountPersistenceMapper,
                                         UserAccountPolicy userAccountPolicy,
                                         UserAccessTokenService userAccessTokenService,
                                         PasswordEncoder passwordEncoder) {
        this.userAccountPersistenceMapper = userAccountPersistenceMapper;
        this.userAccountPolicy = userAccountPolicy;
        this.userAccessTokenService = userAccessTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserSessionResponse register(UserRegisterRequest request) {
        String openId = userAccountPolicy.normalizeOpenId(request.getOpenId());
        String name = userAccountPolicy.normalizeRequiredName(request.getName());
        String phone = userAccountPolicy.normalizePhone(request.getPhone());
        String password = userAccountPolicy.normalizePassword(request.getPassword(), "密码");

        UserAccountDataObject existingUser = userAccountPersistenceMapper.findByOpenId(openId);
        userAccountPolicy.ensureRegisterAvailable(existingUser);

        LocalDateTime now = LocalDateTime.now();
        UserAccountDataObject user = new UserAccountDataObject();
        user.setOpenId(openId);
        user.setName(name);
        user.setPhone(phone);
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
        String openId = userAccountPolicy.normalizeOpenId(request.getOpenId());
        String password = userAccountPolicy.normalizePassword(request.getPassword(), "密码");

        UserAccountDataObject user = userAccountPersistenceMapper.findByOpenId(openId);
        userAccountPolicy.ensureLoginAllowed(user);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }

        return toSessionResponse(user, userAccessTokenService.issueToken(user.getId(), user.getOpenId()));
    }

    @Transactional
    public void resetPassword(UserPasswordResetRequest request) {
        String openId = userAccountPolicy.normalizeOpenId(request.getOpenId());
        String phone = userAccountPolicy.normalizePhone(request.getPhone());
        String newPassword = userAccountPolicy.normalizePassword(request.getNewPassword(), "新密码");

        UserAccountDataObject user = userAccountPersistenceMapper.findByOpenIdAndPhone(openId, phone);
        userAccountPolicy.ensureResetTargetExists(user);

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
        update.setPhone(userAccountPolicy.normalizePhone(request.getPhone()));
        update.setUpdateTime(LocalDateTime.now());

        int affectedRows = userAccountPersistenceMapper.updateProfile(update);
        if (affectedRows <= 0) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "用户账号不存在");
        }

        UserAccountDataObject updated = userAccountPersistenceMapper.findById(authenticatedUser.getUserId());
        return toSessionResponse(updated, authenticatedUser.getAccessToken());
    }

    private UserSessionResponse toSessionResponse(UserAccountDataObject user, String token) {
        return UserSessionResponse.builder()
                .userId(String.valueOf(user.getId()))
                .openId(user.getOpenId())
                .name(user.getName())
                .phone(user.getPhone())
                .token(token)
                .build();
    }
}
