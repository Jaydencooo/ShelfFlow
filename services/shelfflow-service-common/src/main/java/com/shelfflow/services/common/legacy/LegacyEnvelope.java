package com.shelfflow.services.common.legacy;

import lombok.Data;

@Data
public class LegacyEnvelope<T> {
    private Integer code;
    private String msg;
    private T data;
}
