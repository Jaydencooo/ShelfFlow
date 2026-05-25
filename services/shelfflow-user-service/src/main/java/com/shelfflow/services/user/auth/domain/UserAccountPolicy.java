package com.shelfflow.services.user.auth.domain;

import com.shelfflow.services.common.domain.UserAccountStatus;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.user.auth.persistence.dataobject.UserAccountDataObject;
import com.shelfflow.services.user.config.UserAuthProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserAccountPolicy {

    private static final Pattern PASSWORD_COMPLEXITY_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@#$%^&*!._-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

    private final UserAuthProperties userAuthProperties;

    public UserAccountPolicy(UserAuthProperties userAuthProperties) {
        this.userAuthProperties = userAuthProperties;
    }

    public String normalizeOpenId(String openId) {
        if (openId == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "账号不能为空");
        }
        String trimmed = openId.trim();
        if (trimmed.isEmpty()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "账号不能为空");
        }
        if (trimmed.length() < userAuthProperties.getMinAccountLength() || trimmed.length() > userAuthProperties.getMaxAccountLength()) {
            throw new ApplicationException(
                    ErrorCode.VALIDATION_ERROR,
                    String.format("账号长度需在 %d 到 %d 个字符之间", userAuthProperties.getMinAccountLength(), userAuthProperties.getMaxAccountLength())
            );
        }
        return trimmed;
    }

    public String normalizeRequiredName(String name) {
        String normalized = normalizeOptionalText(name);
        if (normalized == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "昵称不能为空");
        }
        if (normalized.length() < userAuthProperties.getMinDisplayNameLength() || normalized.length() > userAuthProperties.getMaxDisplayNameLength()) {
            throw new ApplicationException(
                    ErrorCode.VALIDATION_ERROR,
                    String.format("昵称长度需在 %d 到 %d 个字符之间", userAuthProperties.getMinDisplayNameLength(), userAuthProperties.getMaxDisplayNameLength())
            );
        }
        return normalized;
    }

    public String normalizePhone(String phone) {
        String normalized = normalizeOptionalText(phone);
        if (normalized == null || !PHONE_PATTERN.matcher(normalized).matches()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "手机号格式不正确");
        }
        return normalized;
    }

    public String normalizePassword(String password, String fieldLabel) {
        if (password == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldLabel + "不能为空");
        }
        String trimmed = password.trim();
        if (trimmed.length() < userAuthProperties.getMinPasswordLength() || trimmed.length() > userAuthProperties.getMaxPasswordLength()) {
            throw new ApplicationException(
                    ErrorCode.VALIDATION_ERROR,
                    String.format("%s长度需在 %d 到 %d 个字符之间", fieldLabel, userAuthProperties.getMinPasswordLength(), userAuthProperties.getMaxPasswordLength())
            );
        }
        if (!PASSWORD_COMPLEXITY_PATTERN.matcher(trimmed).matches()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldLabel + "至少包含字母和数字，且只允许常见安全字符");
        }
        return trimmed;
    }

    public void ensureUserExists(boolean exists) {
        if (!exists) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED, "用户会话无效");
        }
    }

    public void ensureRegisterAvailable(UserAccountDataObject existingUser) {
        if (existingUser != null) {
            throw new ApplicationException(ErrorCode.CONFLICT, "账号已存在，请直接登录或找回密码");
        }
    }

    public void ensureLoginAllowed(UserAccountDataObject user) {
        if (user == null || user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (UserAccountStatus.fromCode(user.getStatus()) != UserAccountStatus.ACTIVE) {
            throw new ApplicationException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }
    }

    public void ensureResetTargetExists(UserAccountDataObject user) {
        if (user == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "未找到匹配的用户账号");
        }
        if (UserAccountStatus.fromCode(user.getStatus()) != UserAccountStatus.ACTIVE) {
            throw new ApplicationException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
