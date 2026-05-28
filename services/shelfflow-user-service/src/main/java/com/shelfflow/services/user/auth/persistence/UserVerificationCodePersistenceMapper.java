package com.shelfflow.services.user.auth.persistence;

import com.shelfflow.services.user.auth.persistence.dataobject.UserVerificationCodeDataObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface UserVerificationCodePersistenceMapper {

    void insert(UserVerificationCodeDataObject verificationCode);

    UserVerificationCodeDataObject findLatestAvailable(@Param("target") String target,
                                                       @Param("purpose") String purpose,
                                                       @Param("now") LocalDateTime now);

    int markConsumed(@Param("id") Long id, @Param("consumedAt") LocalDateTime consumedAt);
}
