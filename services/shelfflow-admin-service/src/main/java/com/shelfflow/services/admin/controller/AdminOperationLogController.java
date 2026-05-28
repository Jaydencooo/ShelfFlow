package com.shelfflow.services.admin.controller;

import com.shelfflow.services.admin.operationlog.service.AdminOperationLogApplicationService;
import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminOperationLogQuery;
import com.shelfflow.services.common.dto.AdminOperationLogResponse;
import com.shelfflow.services.common.security.AdminAuthenticatedUser;
import com.shelfflow.services.common.security.AdminPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/operation-logs")
public class AdminOperationLogController {

    private final AdminOperationLogApplicationService operationLogApplicationService;

    public AdminOperationLogController(AdminOperationLogApplicationService operationLogApplicationService) {
        this.operationLogApplicationService = operationLogApplicationService;
    }

    @GetMapping
    public ApiResponse<List<AdminOperationLogResponse>> latest(AdminAuthenticatedUser authenticatedUser,
                                                               @RequestParam(required = false) Integer limit,
                                                               HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.OPERATION_LOG_READ);
        return ApiResponse.success(operationLogApplicationService.latest(limit), requestId(request), "查询成功");
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<AdminOperationLogResponse>> page(AdminAuthenticatedUser authenticatedUser,
                                                                     @Valid @ModelAttribute AdminOperationLogQuery query,
                                                                     HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.OPERATION_LOG_READ);
        return ApiResponse.success(operationLogApplicationService.page(query), requestId(request), "查询成功");
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
