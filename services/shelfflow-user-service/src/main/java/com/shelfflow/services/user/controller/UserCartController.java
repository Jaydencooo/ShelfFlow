package com.shelfflow.services.user.controller;

import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.dto.UserCartItemAddRequest;
import com.shelfflow.services.common.dto.UserCartItemQuantityUpdateRequest;
import com.shelfflow.services.common.dto.UserCartItemResponse;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.cart.service.UserCartApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/user/cart")
public class UserCartController {

    private final UserCartApplicationService userCartApplicationService;

    public UserCartController(UserCartApplicationService userCartApplicationService) {
        this.userCartApplicationService = userCartApplicationService;
    }

    @GetMapping("/items")
    public ApiResponse<List<UserCartItemResponse>> items(UserAuthenticatedUser authenticatedUser, HttpServletRequest request) {
        return ApiResponse.success(
                userCartApplicationService.listItems(authenticatedUser),
                requestId(request),
                "查询成功"
        );
    }

    @PostMapping("/items")
    public ApiResponse<Void> addItem(UserAuthenticatedUser authenticatedUser,
                                     @Valid @RequestBody UserCartItemAddRequest request,
                                     HttpServletRequest servletRequest) {
        userCartApplicationService.addItem(authenticatedUser, request);
        return ApiResponse.success(null, requestId(servletRequest), "加入购物车成功");
    }

    @PatchMapping("/items/{id}")
    public ApiResponse<Void> updateItemQuantity(UserAuthenticatedUser authenticatedUser,
                                                @PathVariable("id") String id,
                                                @Valid @RequestBody UserCartItemQuantityUpdateRequest request,
                                                HttpServletRequest servletRequest) {
        userCartApplicationService.updateItemQuantity(authenticatedUser, id, request);
        return ApiResponse.success(null, requestId(servletRequest), "更新购物车数量成功");
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> removeItem(UserAuthenticatedUser authenticatedUser,
                                        @PathVariable("id") String id,
                                        HttpServletRequest request) {
        userCartApplicationService.removeItem(authenticatedUser, id);
        return ApiResponse.success(null, requestId(request), "删除购物车项成功");
    }

    @DeleteMapping("/items")
    public ApiResponse<Void> clear(UserAuthenticatedUser authenticatedUser, HttpServletRequest request) {
        userCartApplicationService.clear(authenticatedUser);
        return ApiResponse.success(null, requestId(request), "清空购物车成功");
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
