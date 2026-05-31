package com.shelfflow.services.user.order.persistence;

import com.shelfflow.services.user.order.persistence.dataobject.UserOrderEventOutboxDataObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserOrderEventOutboxPersistenceMapper {

    int insert(UserOrderEventOutboxDataObject outbox);

    List<UserOrderEventOutboxDataObject> listPendingDue(@Param("now") LocalDateTime now,
                                                        @Param("limit") int limit,
                                                        @Param("maxAttempts") int maxAttempts);

    int markPublished(@Param("id") Long id,
                      @Param("publishedTime") LocalDateTime publishedTime,
                      @Param("updateTime") LocalDateTime updateTime);

    int markFailed(@Param("id") Long id,
                   @Param("nextRetryTime") LocalDateTime nextRetryTime,
                   @Param("lastError") String lastError,
                   @Param("updateTime") LocalDateTime updateTime);
}
