package com.shelfflow.service;

import com.shelfflow.dto.StaffDTO;
import com.shelfflow.dto.StaffLoginDTO;
import com.shelfflow.dto.StaffPageQueryDTO;
import com.shelfflow.entity.Staff;
import com.shelfflow.result.PageResult;

public interface StaffService {

    /**
     * 运营人员登录
     * @param staffLoginDTO
     * @return
     */
    Staff login(StaffLoginDTO staffLoginDTO);

    //新增运营人员
    void add(StaffDTO staffDTO);

    PageResult pageQuery(StaffPageQueryDTO staffPageQueryDTO);

    void changeStatus(Integer status, Long id);

    Staff getById(Integer id);

    void update(StaffDTO staffDTO);
}
