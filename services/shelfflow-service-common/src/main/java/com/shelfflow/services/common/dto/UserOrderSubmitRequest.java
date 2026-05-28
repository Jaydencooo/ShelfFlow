package com.shelfflow.services.common.dto;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class UserOrderSubmitRequest {
    @Size(max = 100)
    private String remark;

    private String pickupContactId;

    @Size(max = 64)
    private String pickupPointId;

    private List<String> cartItemIds;
}
