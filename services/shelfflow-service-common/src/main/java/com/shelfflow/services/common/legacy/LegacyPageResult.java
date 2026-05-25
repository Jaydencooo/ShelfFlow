package com.shelfflow.services.common.legacy;

import lombok.Data;

import java.util.List;

@Data
public class LegacyPageResult<T> {
    private long total;
    private List<T> records;
}
