package com.shelfflow.services.common.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

public final class StorefrontCatalogCacheKeys {

    public static final String DEFAULT_PREFIX = "shelfflow:storefront:catalog";
    public static final String CATEGORY_SEGMENT = "categories";
    public static final String PRODUCT_SEGMENT = "products";
    private static final String KEY_SEPARATOR = ":";
    private static final String HASH_ALGORITHM = "SHA-256";

    private StorefrontCatalogCacheKeys() {
    }

    public static String categoriesKey(String prefix) {
        return normalizePrefix(prefix) + KEY_SEPARATOR + CATEGORY_SEGMENT;
    }

    public static String productsKey(String prefix,
                                     String keyword,
                                     Long categoryId,
                                     String sortColumn,
                                     String sortDirection,
                                     int page,
                                     int pageSize) {
        String raw = "keyword=" + normalizeNullableText(keyword)
                + "|categoryId=" + (categoryId == null ? "" : categoryId)
                + "|sortColumn=" + normalizeNullableText(sortColumn)
                + "|sortDirection=" + normalizeNullableText(sortDirection)
                + "|page=" + page
                + "|pageSize=" + pageSize;
        return normalizePrefix(prefix) + KEY_SEPARATOR + PRODUCT_SEGMENT + KEY_SEPARATOR + sha256(raw);
    }

    public static String allKeysPattern(String prefix) {
        return normalizePrefix(prefix) + KEY_SEPARATOR + "*";
    }

    public static String normalizePrefix(String prefix) {
        String normalized = prefix == null ? "" : prefix.trim();
        return normalized.isEmpty() ? DEFAULT_PREFIX : trimTrailingSeparators(normalized);
    }

    private static String normalizeNullableText(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String trimTrailingSeparators(String value) {
        String normalized = value;
        while (normalized.endsWith(KEY_SEPARATOR)) {
            normalized = normalized.substring(0, normalized.length() - KEY_SEPARATOR.length());
        }
        return normalized;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}
