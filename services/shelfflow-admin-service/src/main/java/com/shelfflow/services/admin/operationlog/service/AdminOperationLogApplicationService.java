package com.shelfflow.services.admin.operationlog.service;

import com.shelfflow.services.admin.operationlog.persistence.AdminOperationLogPersistenceMapper;
import com.shelfflow.services.admin.operationlog.persistence.dataobject.AdminOperationLogDataObject;
import com.shelfflow.services.admin.operationlog.persistence.dataobject.AdminOperationLogPageCriteria;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminOperationLogQuery;
import com.shelfflow.services.common.dto.AdminOperationLogResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminOperationLogApplicationService {

    private static final int DEFAULT_LATEST_LIMIT = 10;
    private static final int MAX_LATEST_LIMIT = 50;

    private final AdminOperationLogPersistenceMapper operationLogPersistenceMapper;

    public AdminOperationLogApplicationService(AdminOperationLogPersistenceMapper operationLogPersistenceMapper) {
        this.operationLogPersistenceMapper = operationLogPersistenceMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long actorId, String module, String action, String method, String path, Integer statusCode, String requestId) {
        AdminOperationLogDataObject operationLog = new AdminOperationLogDataObject();
        operationLog.setActorId(actorId);
        operationLog.setModule(module);
        operationLog.setAction(action);
        operationLog.setMethod(method);
        operationLog.setPath(path);
        operationLog.setStatusCode(statusCode);
        operationLog.setRequestId(requestId);
        operationLog.setSummary(buildSummary(module, action, statusCode));
        operationLog.setCreateTime(LocalDateTime.now());
        operationLogPersistenceMapper.insert(operationLog);
    }

    @Transactional(readOnly = true)
    public List<AdminOperationLogResponse> latest(Integer limit) {
        int resolvedLimit = resolveLimit(limit);
        return operationLogPersistenceMapper.latest(resolvedLimit).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminOperationLogResponse> page(AdminOperationLogQuery query) {
        AdminOperationLogPageCriteria criteria = AdminOperationLogPageCriteria.builder()
                .module(blankToNull(query.getModule()))
                .action(blankToNull(query.getAction()))
                .offset((query.getPage() - 1) * query.getPageSize())
                .pageSize(query.getPageSize())
                .build();
        List<AdminOperationLogResponse> items = operationLogPersistenceMapper.page(criteria).stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.<AdminOperationLogResponse>builder()
                .items(items)
                .total(operationLogPersistenceMapper.count(criteria))
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LATEST_LIMIT;
        }
        return Math.min(Math.max(limit, 1), MAX_LATEST_LIMIT);
    }

    private String buildSummary(String module, String action, Integer statusCode) {
        String result = statusCode != null && statusCode >= 200 && statusCode < 400 ? "成功" : "失败";
        return module + " " + action + " " + result;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private AdminOperationLogResponse toResponse(AdminOperationLogDataObject row) {
        return AdminOperationLogResponse.builder()
                .id(String.valueOf(row.getId()))
                .module(row.getModule())
                .action(row.getAction())
                .method(row.getMethod())
                .path(row.getPath())
                .statusCode(row.getStatusCode())
                .actorId(row.getActorId())
                .summary(row.getSummary())
                .createTime(row.getCreateTime())
                .build();
    }
}
