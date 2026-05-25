package com.shelfflow.services.admin.controller;

import com.shelfflow.services.admin.lossstats.service.AdminLossStatsApplicationService;
import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.dto.AdminLossStatsOverviewResponse;
import com.shelfflow.services.common.security.AdminAuthenticatedUser;
import com.shelfflow.services.common.security.AdminPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/loss-stats")
public class AdminLossStatsController {

    private final AdminLossStatsApplicationService lossStatsApplicationService;

    public AdminLossStatsController(AdminLossStatsApplicationService lossStatsApplicationService) {
        this.lossStatsApplicationService = lossStatsApplicationService;
    }

    @GetMapping("/overview")
    public ApiResponse<AdminLossStatsOverviewResponse> overview(AdminAuthenticatedUser authenticatedUser,
                                                                HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.LOSS_STATS_READ);
        return ApiResponse.success(lossStatsApplicationService.overview(), requestId(request), "查询成功");
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
