package com.shelfflow.services.user.auth.persistence.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAccountDataObject {
    private Long id;
    private String openId;
    private String name;
    private String phone;
    private String email;
    private String passwordHash;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
