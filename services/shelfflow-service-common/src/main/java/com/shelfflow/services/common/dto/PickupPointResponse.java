package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PickupPointResponse {
    private String id;
    private String name;
    private String address;
    private String contactName;
    private String contactPhone;
    private String serviceTime;
    private Integer sort;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
