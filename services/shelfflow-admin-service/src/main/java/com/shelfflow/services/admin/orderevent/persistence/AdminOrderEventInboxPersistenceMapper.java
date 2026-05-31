package com.shelfflow.services.admin.orderevent.persistence;

import com.shelfflow.services.admin.orderevent.persistence.dataobject.AdminOrderEventInboxDataObject;
import com.shelfflow.services.admin.orderevent.persistence.dataobject.AdminOrderEventInboxPageCriteria;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminOrderEventInboxPersistenceMapper {

    int insert(AdminOrderEventInboxDataObject inbox);

    AdminOrderEventInboxDataObject findByEventId(@Param("eventId") Long eventId);

    int markProcessed(@Param("id") Long id,
                      @Param("processedTime") LocalDateTime processedTime,
                      @Param("updateTime") LocalDateTime updateTime);

    int markFailed(@Param("id") Long id,
                   @Param("failureReason") String failureReason,
                   @Param("updateTime") LocalDateTime updateTime);

    List<AdminOrderEventInboxDataObject> page(AdminOrderEventInboxPageCriteria criteria);

    long count(AdminOrderEventInboxPageCriteria criteria);
}
