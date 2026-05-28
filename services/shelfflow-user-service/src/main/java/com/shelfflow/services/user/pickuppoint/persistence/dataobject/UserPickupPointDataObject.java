package com.shelfflow.services.user.pickuppoint.persistence.dataobject;

import lombok.Data;

@Data
public class UserPickupPointDataObject {
    private Long id;
    private String name;
    private String address;
    private String contactName;
    private String contactPhone;
    private String serviceTime;
    private Integer sort;
    private Integer status;
}
