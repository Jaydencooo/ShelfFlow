package com.shelfflow.services.admin.controller;

import com.shelfflow.services.admin.pickuppoint.service.AdminPickupPointApplicationService;
import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.dto.PickupPointResponse;
import com.shelfflow.services.common.dto.PickupPointUpsertRequest;
import com.shelfflow.services.common.security.AdminAuthenticatedUser;
import com.shelfflow.services.common.security.AdminPermission;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pickup-points")
public class AdminPickupPointController {

    private final AdminPickupPointApplicationService pickupPointApplicationService;

    public AdminPickupPointController(AdminPickupPointApplicationService pickupPointApplicationService) {
        this.pickupPointApplicationService = pickupPointApplicationService;
    }

    @GetMapping
    public ApiResponse<List<PickupPointResponse>> list(AdminAuthenticatedUser authenticatedUser,
                                                       HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.ORDER_READ);
        return ApiResponse.success(pickupPointApplicationService.listAll(), requestId(request), "查询成功");
    }

    @PostMapping
    public ApiResponse<PickupPointResponse> create(AdminAuthenticatedUser authenticatedUser,
                                                   @Valid @RequestBody PickupPointUpsertRequest requestBody,
                                                   HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.ORDER_WRITE);
        return ApiResponse.success(
                pickupPointApplicationService.create(authenticatedUser.getUserId(), requestBody),
                requestId(request),
                "自提点创建成功"
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<PickupPointResponse> update(AdminAuthenticatedUser authenticatedUser,
                                                   @PathVariable String id,
                                                   @Valid @RequestBody PickupPointUpsertRequest requestBody,
                                                   HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.ORDER_WRITE);
        return ApiResponse.success(
                pickupPointApplicationService.update(authenticatedUser.getUserId(), id, requestBody),
                requestId(request),
                "自提点更新成功"
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> disable(AdminAuthenticatedUser authenticatedUser,
                                     @PathVariable String id,
                                     HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.ORDER_WRITE);
        pickupPointApplicationService.disable(authenticatedUser.getUserId(), id);
        return ApiResponse.success(null, requestId(request), "自提点已停用");
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
