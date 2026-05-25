package com.shelfflow.services.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPickupContactResponse {
    private String id;
    private String consignee;
    private String phone;
    private String label;
    private String detail;
    private boolean defaultContact;
}
