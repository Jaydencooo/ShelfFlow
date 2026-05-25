package com.shelfflow.services.admin.controller;

import com.shelfflow.services.admin.inventorybatch.service.AdminInventoryBatchApplicationService;
import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.BatchStatusUpdateRequest;
import com.shelfflow.services.common.dto.InventoryBatchQuery;
import com.shelfflow.services.common.dto.InventoryBatchRecordResponse;
import com.shelfflow.services.common.dto.InventoryBatchUpsertRequest;
import com.shelfflow.services.common.security.AdminPermission;
import com.shelfflow.services.common.security.AdminAuthenticatedUser;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/inventory-batches")
public class AdminInventoryBatchController {
    private final AdminInventoryBatchApplicationService adminInventoryBatchApplicationService;

    public AdminInventoryBatchController(AdminInventoryBatchApplicationService adminInventoryBatchApplicationService) {
        this.adminInventoryBatchApplicationService = adminInventoryBatchApplicationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<InventoryBatchRecordResponse>> page(AdminAuthenticatedUser authenticatedUser,
                                                                        InventoryBatchQuery query,
                                                                        HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.INVENTORY_READ);
        return ApiResponse.success(adminInventoryBatchApplicationService.page(query), requestId(request), "查询成功");
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryBatchRecordResponse> getById(AdminAuthenticatedUser authenticatedUser,
                                                             @PathVariable Long id,
                                                             HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.INVENTORY_READ);
        return ApiResponse.success(adminInventoryBatchApplicationService.getById(id), requestId(request), "查询成功");
    }

    @PostMapping
    public ApiResponse<Void> create(AdminAuthenticatedUser authenticatedUser,
                                    @Valid @RequestBody InventoryBatchUpsertRequest requestBody,
                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.INVENTORY_WRITE);
        adminInventoryBatchApplicationService.create(authenticatedUser.getUserId(), requestBody);
        return ApiResponse.success(null, requestId(request), "批次创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(AdminAuthenticatedUser authenticatedUser,
                                    @PathVariable String id,
                                    @Valid @RequestBody InventoryBatchUpsertRequest requestBody,
                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.INVENTORY_WRITE);
        adminInventoryBatchApplicationService.update(authenticatedUser.getUserId(), id, requestBody);
        return ApiResponse.success(null, requestId(request), "批次更新成功");
    }

    @PostMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(AdminAuthenticatedUser authenticatedUser,
                                          @PathVariable Long id,
                                          @Valid @RequestBody BatchStatusUpdateRequest requestBody,
                                          HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.INVENTORY_WRITE);
        adminInventoryBatchApplicationService.updateStatus(authenticatedUser.getUserId(), id, requestBody.getBatchStatus());
        return ApiResponse.success(null, requestId(request), "批次状态更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(AdminAuthenticatedUser authenticatedUser,
                                    @PathVariable Long id,
                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.INVENTORY_WRITE);
        adminInventoryBatchApplicationService.delete(id);
        return ApiResponse.success(null, requestId(request), "批次已删除");
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
