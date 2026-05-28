package com.shelfflow.services.admin.controller;

import com.shelfflow.services.admin.order.service.AdminOrderFulfillmentApplicationService;
import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminOrderDetailResponse;
import com.shelfflow.services.common.dto.AdminOrderPickupVerifyRequest;
import com.shelfflow.services.common.dto.AdminOrderQuery;
import com.shelfflow.services.common.dto.AdminOrderStatusUpdateRequest;
import com.shelfflow.services.common.dto.AdminOrderSummaryResponse;
import com.shelfflow.services.common.security.AdminAuthenticatedUser;
import com.shelfflow.services.common.security.AdminPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderFulfillmentApplicationService adminOrderFulfillmentApplicationService;

    public AdminOrderController(AdminOrderFulfillmentApplicationService adminOrderFulfillmentApplicationService) {
        this.adminOrderFulfillmentApplicationService = adminOrderFulfillmentApplicationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminOrderSummaryResponse>> page(AdminAuthenticatedUser authenticatedUser,
                                                                     @Valid @ModelAttribute AdminOrderQuery query,
                                                                     HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.ORDER_READ);
        return ApiResponse.success(adminOrderFulfillmentApplicationService.page(query), requestId(request), "查询成功");
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminOrderDetailResponse> detail(AdminAuthenticatedUser authenticatedUser,
                                                        @PathVariable String id,
                                                        HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.ORDER_READ);
        return ApiResponse.success(adminOrderFulfillmentApplicationService.getById(id), requestId(request), "查询成功");
    }

    @PostMapping("/{id}/status")
    public ApiResponse<AdminOrderDetailResponse> updateStatus(AdminAuthenticatedUser authenticatedUser,
                                                              @PathVariable String id,
                                                              @Valid @RequestBody AdminOrderStatusUpdateRequest requestBody,
                                                              HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.ORDER_WRITE);
        return ApiResponse.success(
                adminOrderFulfillmentApplicationService.updateStatus(authenticatedUser.getUserId(), id, requestBody.getOrderStatus()),
                requestId(request),
                "订单状态更新成功"
        );
    }

    @PostMapping("/{id}/pickup-verification")
    public ApiResponse<AdminOrderDetailResponse> verifyPickup(AdminAuthenticatedUser authenticatedUser,
                                                              @PathVariable String id,
                                                              @Valid @RequestBody AdminOrderPickupVerifyRequest requestBody,
                                                              HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.ORDER_WRITE);
        return ApiResponse.success(
                adminOrderFulfillmentApplicationService.verifyPickup(authenticatedUser.getUserId(), id, requestBody.getPickupCode()),
                requestId(request),
                "订单自提核销成功"
        );
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
