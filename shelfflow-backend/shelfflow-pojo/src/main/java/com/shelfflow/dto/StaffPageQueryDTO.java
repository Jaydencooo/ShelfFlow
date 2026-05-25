package com.shelfflow.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StaffPageQueryDTO implements Serializable {

    //运营人员姓名
    private String name;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
