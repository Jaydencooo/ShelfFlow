package com.shelfflow.services.user.controller;

import com.shelfflow.services.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@RestController
public class UserHealthController {

    @GetMapping("/api/user/health")
    public ApiResponse<Object> health(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return ApiResponse.success(Collections.singletonMap("ready", Boolean.TRUE),
                requestId == null || requestId.isBlank() ? "unknown" : requestId,
                "用户服务已就绪");
    }
}
