package com.shelfflow.services.user.auth.domain;

import com.shelfflow.services.common.domain.UserAccountStatus;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.user.auth.persistence.dataobject.UserAccountDataObject;
import com.shelfflow.services.user.config.UserAuthProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class UserAccountPolicy {

    private static final Pattern PASSWORD_COMPLEXITY_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@#$%^&*!._-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile("^\\d{4,10}$");

    public static final String VERIFICATION_PURPOSE_REGISTER = "register";
    public static final String VERIFICATION_PURPOSE_RESET_PASSWORD = "reset_password";
    public static final String VERIFICATION_PURPOSE_CHANGE_PHONE = "change_phone";
    public static final String VERIFICATION_PURPOSE_CHANGE_EMAIL = "change_email";

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

    public String normalizeLoginAccount(String account, String legacyOpenId) {
        String rawAccount = normalizeOptionalText(account);
        if (rawAccount == null) {
            rawAccount = normalizeOptionalText(legacyOpenId);
        }
        if (rawAccount == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "登录账号不能为空");
        }
        if (isPhone(rawAccount)) {
            return rawAccount;
        }
        if (isEmail(rawAccount)) {
            return rawAccount.toLowerCase(Locale.ROOT);
        }
        return normalizeOpenId(rawAccount);
    }

    public RegistrationAccount normalizeRegistrationAccount(String account, String legacyOpenId, String phone, String email) {
        String rawAccount = firstNonBlank(account, legacyOpenId, phone, email);
        if (rawAccount == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "请填写邮箱或手机号");
        }

        if (isPhone(rawAccount)) {
            return new RegistrationAccount(rawAccount, rawAccount, null);
        }
        if (isEmail(rawAccount)) {
            String normalizedEmail = rawAccount.toLowerCase(Locale.ROOT);
            return new RegistrationAccount(normalizedEmail, null, normalizedEmail);
        }
        throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "注册账号必须是手机号或邮箱");
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

    public String normalizeOptionalPhone(String phone) {
        String normalized = normalizeOptionalText(phone);
        if (normalized == null) {
            return null;
        }
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "手机号格式不正确");
        }
        return normalized;
    }

    public String normalizeOptionalEmail(String email) {
        String normalized = normalizeOptionalText(email);
        if (normalized == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "邮箱格式不正确");
        }
        return normalized.toLowerCase(Locale.ROOT);
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

    public void ensurePasswordConfirmed(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "两次输入的密码不一致");
        }
    }

    public String normalizeVerificationPurpose(String purpose) {
        String normalized = normalizeOptionalText(purpose);
        if (VERIFICATION_PURPOSE_REGISTER.equals(normalized)
                || VERIFICATION_PURPOSE_RESET_PASSWORD.equals(normalized)
                || VERIFICATION_PURPOSE_CHANGE_PHONE.equals(normalized)
                || VERIFICATION_PURPOSE_CHANGE_EMAIL.equals(normalized)) {
            return normalized;
        }
        throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "验证码用途不合法");
    }

    public String normalizeVerificationCode(String verificationCode) {
        String normalized = normalizeOptionalText(verificationCode);
        if (normalized == null || !VERIFICATION_CODE_PATTERN.matcher(normalized).matches()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "验证码格式不正确");
        }
        return normalized;
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

    public void ensurePhoneAvailable(UserAccountDataObject existingUser) {
        if (existingUser != null) {
            throw new ApplicationException(ErrorCode.CONFLICT, "手机号已注册，请直接登录或找回密码");
        }
    }

    public void ensurePhoneAvailableForCurrentUser(UserAccountDataObject existingUser, Long currentUserId) {
        if (existingUser != null && !existingUser.getId().equals(currentUserId)) {
            throw new ApplicationException(ErrorCode.CONFLICT, "手机号已被其他账号绑定");
        }
    }

    public void ensureEmailAvailable(UserAccountDataObject existingUser) {
        if (existingUser != null) {
            throw new ApplicationException(ErrorCode.CONFLICT, "邮箱已注册，请直接登录或找回密码");
        }
    }

    public void ensureEmailAvailableForCurrentUser(UserAccountDataObject existingUser, Long currentUserId) {
        if (existingUser != null && !existingUser.getId().equals(currentUserId)) {
            throw new ApplicationException(ErrorCode.CONFLICT, "邮箱已被其他账号绑定");
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

    private boolean isPhone(String value) {
        return PHONE_PATTERN.matcher(value).matches();
    }

    private boolean isEmail(String value) {
        return EMAIL_PATTERN.matcher(value).matches();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = normalizeOptionalText(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    public record RegistrationAccount(String account, String phone, String email) {
    }
}
