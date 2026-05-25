package com.shelfflow.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StaffDTO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

}
