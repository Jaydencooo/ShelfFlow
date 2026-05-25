package com.shelfflow.services.user.pickupcontact.persistence;

import com.shelfflow.services.user.pickupcontact.persistence.dataobject.UserPickupContactDataObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPickupContactPersistenceMapper {

    List<UserPickupContactDataObject> listByUserId(@Param("userId") Long userId);

    UserPickupContactDataObject findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int countByUserId(@Param("userId") Long userId);

    void insert(UserPickupContactDataObject contact);

    int updateByIdAndUserId(UserPickupContactDataObject contact);

    int clearDefaultByUserId(@Param("userId") Long userId);

    int setDefaultByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
