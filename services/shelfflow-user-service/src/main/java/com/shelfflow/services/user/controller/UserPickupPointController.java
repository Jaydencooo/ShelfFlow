package com.shelfflow.services.user.controller;

import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.dto.PickupPointResponse;
import com.shelfflow.services.user.pickuppoint.service.UserPickupPointApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/pickup-points")
public class UserPickupPointController {

    private final UserPickupPointApplicationService pickupPointApplicationService;

    public UserPickupPointController(UserPickupPointApplicationService pickupPointApplicationService) {
        this.pickupPointApplicationService = pickupPointApplicationService;
    }

    @GetMapping
    public ApiResponse<List<PickupPointResponse>> list(HttpServletRequest request) {
        return ApiResponse.success(pickupPointApplicationService.listEnabled(), requestId(request), "查询成功");
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
