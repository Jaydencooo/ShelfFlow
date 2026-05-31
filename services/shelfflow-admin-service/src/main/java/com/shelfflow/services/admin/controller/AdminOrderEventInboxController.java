package com.shelfflow.services.admin.controller;

import com.shelfflow.services.admin.orderevent.service.AdminOrderEventInboxApplicationService;
import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminOrderEventInboxQuery;
import com.shelfflow.services.common.dto.AdminOrderEventInboxResponse;
import com.shelfflow.services.common.security.AdminAuthenticatedUser;
import com.shelfflow.services.common.security.AdminPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/order-event-inbox")
public class AdminOrderEventInboxController {

    private final AdminOrderEventInboxApplicationService inboxApplicationService;

    public AdminOrderEventInboxController(AdminOrderEventInboxApplicationService inboxApplicationService) {
        this.inboxApplicationService = inboxApplicationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminOrderEventInboxResponse>> page(AdminAuthenticatedUser authenticatedUser,
                                                                        @Valid @ModelAttribute AdminOrderEventInboxQuery query,
                                                                        HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.ORDER_READ);
        return ApiResponse.success(inboxApplicationService.page(query), requestId(request), "查询成功");
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
