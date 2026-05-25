package com.shelfflow.services.user.pickupcontact.domain;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserPickupContactPolicy {

    private static final int MAX_CONTACT_COUNT = 10;
    private static final int MIN_CONSIGNEE_LENGTH = 2;
    private static final int MAX_CONSIGNEE_LENGTH = 32;
    private static final int MAX_LABEL_LENGTH = 16;
    private static final int MAX_DETAIL_LENGTH = 120;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

    public Long parseRequiredContactId(String value) {
        if (value == null || value.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "联系人 ID 不能为空");
        }
        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                throw new NumberFormatException("id must be positive");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "联系人 ID 不合法");
        }
    }

    public String normalizeConsignee(String value) {
        String normalized = normalizeRequiredText(value, "联系人不能为空");
        if (normalized.length() < MIN_CONSIGNEE_LENGTH || normalized.length() > MAX_CONSIGNEE_LENGTH) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "联系人长度需在 2 到 32 个字符之间");
        }
        return normalized;
    }

    public String normalizePhone(String value) {
        String normalized = normalizeRequiredText(value, "手机号不能为空");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "手机号格式不正确");
        }
        return normalized;
    }

    public String normalizeLabel(String value) {
        return normalizeOptionalText(value, MAX_LABEL_LENGTH, "标签不能超过 16 个字符");
    }

    public String normalizeDetail(String value) {
        return normalizeOptionalText(value, MAX_DETAIL_LENGTH, "备注不能超过 120 个字符");
    }

    public void ensureContactLimitNotExceeded(int currentCount) {
        if (currentCount >= MAX_CONTACT_COUNT) {
            throw new ApplicationException(ErrorCode.CONFLICT, "自提联系人最多保留 10 个");
        }
    }

    public void ensureContactExists(boolean exists) {
        if (!exists) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "自提联系人不存在");
        }
    }

    private String normalizeRequiredText(String value, String message) {
        String normalized = normalizeOptionalRawText(value);
        if (normalized == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, message);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value, int maxLength, String message) {
        String normalized = normalizeOptionalRawText(value);
        if (normalized != null && normalized.length() > maxLength) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, message);
        }
        return normalized;
    }

    private String normalizeOptionalRawText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
