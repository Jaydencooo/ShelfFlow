package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserCatalogProductSpecResponse {
    private String name;
    private List<String> values;
}
