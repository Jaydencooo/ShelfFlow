package com.shelfflow.mapper;

import com.github.pagehelper.Page;
import com.shelfflow.anno.AutoFill;
import com.shelfflow.dto.StaffPageQueryDTO;
import com.shelfflow.entity.Staff;
import com.shelfflow.enumeration.OperationType;
import com.shelfflow.result.PageResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StaffMapper {

    /**
     * 根据用户名查询运营人员
     * @param username
     * @return
     */
    @Select("select * from staff where username = #{username}")
    Staff getByUsername(String username);

    @AutoFill(value= OperationType.INSERT)
    @Insert("insert into staff(name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) values(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser}) ")
    void add(Staff staff);


    Page<Staff> pageQuery(StaffPageQueryDTO staffPageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    void update(Staff staff);

    @Select("select * from staff where id = #{id}")
    Staff getById(Integer id);
}
