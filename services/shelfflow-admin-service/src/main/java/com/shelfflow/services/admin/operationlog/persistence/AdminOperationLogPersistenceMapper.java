package com.shelfflow.services.admin.operationlog.persistence;

import com.shelfflow.services.admin.operationlog.persistence.dataobject.AdminOperationLogDataObject;
import com.shelfflow.services.admin.operationlog.persistence.dataobject.AdminOperationLogPageCriteria;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminOperationLogPersistenceMapper {

    int insert(AdminOperationLogDataObject operationLog);

    List<AdminOperationLogDataObject> latest(@Param("limit") int limit);

    List<AdminOperationLogDataObject> page(AdminOperationLogPageCriteria criteria);

    long count(AdminOperationLogPageCriteria criteria);
}
