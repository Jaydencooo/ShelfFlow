package com.shelfflow.services.user.catalog.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.dto.UserCatalogProductSpecResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogProductSpecRow;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserCatalogSpecParser {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public UserCatalogSpecParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UserCatalogProductSpecResponse toResponse(UserCatalogProductSpecRow row) {
        return UserCatalogProductSpecResponse.builder()
                .name(row.getName())
                .values(parseValues(row.getValue()))
                .build();
    }

    private List<String> parseValues(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(rawValue, STRING_LIST_TYPE);
        } catch (Exception exception) {
            throw new ApplicationException(ErrorCode.INTERNAL_ERROR, "商品规格数据格式无效");
        }
    }
}
