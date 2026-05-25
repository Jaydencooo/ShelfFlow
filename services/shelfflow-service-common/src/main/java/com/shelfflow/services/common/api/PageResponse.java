package com.shelfflow.services.common.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageResponse<T> {
    private List<T> items;
    private long total;
    private int page;
    private int pageSize;
}
