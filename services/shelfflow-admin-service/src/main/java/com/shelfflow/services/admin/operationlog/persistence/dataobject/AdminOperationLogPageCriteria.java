package com.shelfflow.services.admin.operationlog.persistence.dataobject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminOperationLogPageCriteria {
    private String module;
    private String action;
    private int offset;
    private int pageSize;
}
