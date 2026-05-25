package com.shelfflow.services.user.pickupcontact.persistence.dataobject;

import lombok.Data;

@Data
public class UserPickupContactDataObject {
    private Long id;
    private Long userId;
    private String consignee;
    private String phone;
    private String label;
    private String detail;
    private Integer isDefault;
}
