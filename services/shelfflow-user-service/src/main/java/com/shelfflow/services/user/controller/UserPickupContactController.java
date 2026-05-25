package com.shelfflow.services.user.controller;

import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.dto.UserPickupContactRequest;
import com.shelfflow.services.common.dto.UserPickupContactResponse;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.pickupcontact.service.UserPickupContactApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@Validated
@RequestMapping("/api/user/pickup-contacts")
public class UserPickupContactController {

    private final UserPickupContactApplicationService userPickupContactApplicationService;

    public UserPickupContactController(UserPickupContactApplicationService userPickupContactApplicationService) {
        this.userPickupContactApplicationService = userPickupContactApplicationService;
    }

    @GetMapping
    public ApiResponse<List<UserPickupContactResponse>> list(UserAuthenticatedUser authenticatedUser, HttpServletRequest request) {
        return ApiResponse.success(
                userPickupContactApplicationService.list(authenticatedUser),
                requestId(request),
                "查询成功"
        );
    }

    @PostMapping
    public ApiResponse<UserPickupContactResponse> create(UserAuthenticatedUser authenticatedUser,
                                                         @Valid @RequestBody UserPickupContactRequest request,
                                                         HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userPickupContactApplicationService.create(authenticatedUser, request),
                requestId(servletRequest),
                "自提联系人创建成功"
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<UserPickupContactResponse> update(UserAuthenticatedUser authenticatedUser,
                                                         @PathVariable("id") String id,
                                                         @Valid @RequestBody UserPickupContactRequest request,
                                                         HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userPickupContactApplicationService.update(authenticatedUser, id, request),
                requestId(servletRequest),
                "自提联系人更新成功"
        );
    }

    @PatchMapping("/{id}/default")
    public ApiResponse<UserPickupContactResponse> setDefault(UserAuthenticatedUser authenticatedUser,
                                                             @PathVariable("id") String id,
                                                             HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userPickupContactApplicationService.setDefault(authenticatedUser, id),
                requestId(servletRequest),
                "默认联系人设置成功"
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(UserAuthenticatedUser authenticatedUser,
                                    @PathVariable("id") String id,
                                    HttpServletRequest servletRequest) {
        userPickupContactApplicationService.delete(authenticatedUser, id);
        return ApiResponse.success(null, requestId(servletRequest), "自提联系人删除成功");
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
