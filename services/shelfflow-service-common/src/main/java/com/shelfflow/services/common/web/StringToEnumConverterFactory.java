package com.shelfflow.services.common.web;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static final class StringToEnumConverter<T extends Enum> implements Converter<String, T> {
        private final Class<T> targetType;

        private StringToEnumConverter(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(String source) {
            if (source == null) {
                return null;
            }

            String normalized = source.trim();
            if (normalized.isEmpty()) {
                return null;
            }

            T resolved = resolveWithFromValue(normalized);
            if (resolved != null) {
                return resolved;
            }

            return (T) Enum.valueOf((Class) targetType, normalized.toUpperCase(Locale.ROOT));
        }

        private T resolveWithFromValue(String source) {
            try {
                Method fromValue = targetType.getMethod("fromValue", String.class);
                Object value = fromValue.invoke(null, source);
                return targetType.cast(value);
            } catch (NoSuchMethodException ignored) {
                return null;
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new IllegalArgumentException("Unsupported enum value: " + source, exception);
            }
        }
    }
}
