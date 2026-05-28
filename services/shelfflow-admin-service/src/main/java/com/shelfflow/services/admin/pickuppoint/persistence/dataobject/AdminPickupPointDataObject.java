package com.shelfflow.services.admin.pickuppoint.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPickupPointDataObject {
    private Long id;
    private String name;
    private String address;
    private String contactName;
    private String contactPhone;
    private String serviceTime;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
