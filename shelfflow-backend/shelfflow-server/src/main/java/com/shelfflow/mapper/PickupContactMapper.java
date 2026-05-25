package com.shelfflow.mapper;

import com.shelfflow.entity.PickupContact;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PickupContactMapper {
    @Insert("insert into pickup_contact(user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) " +
            "VALUES(#{userId},#{consignee},#{sex},#{phone},#{provinceCode},#{provinceName},#{cityCode},#{cityName},#{districtCode},#{districtName},#{detail},#{label},#{isDefault}) ")
    void insert(PickupContact pickupContact);

    List<PickupContact> list(PickupContact pickupContact);

    void update(PickupContact pickupContact);

    @Select("select * from pickup_contact where id = #{id}")
    PickupContact getById(Long id);

    @Delete("delete from pickup_contact where id = #{id}")
    void deleteById(Long id);
}
