package com.shelfflow.services.admin.aiops.persistence.dataobject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAiKnowledgeCriteria {
    private int limit;
    private int offset;
    private String keyword;
    private String category;
    private String sortColumn;
    private String sortDirection;
}
