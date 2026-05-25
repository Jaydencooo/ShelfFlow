package com.shelfflow.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PasswordEditDTO implements Serializable {

    //运营人员id
    private Long empId;

    //旧密码
    private String oldPassword;

    //新密码
    private String newPassword;

}
