package com.shelfflow.services.user.auth.persistence;

import com.shelfflow.services.user.auth.persistence.dataobject.UserAccountDataObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountPersistenceMapper {

    UserAccountDataObject findByOpenId(@Param("openId") String openId);

    UserAccountDataObject findById(@Param("id") Long id);

    UserAccountDataObject findByOpenIdAndPhone(@Param("openId") String openId, @Param("phone") String phone);

    void insert(UserAccountDataObject userAccount);

    int updateProfile(UserAccountDataObject userAccount);

    int updatePasswordById(UserAccountDataObject userAccount);
}
