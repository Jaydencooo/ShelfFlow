package com.shelfflow.services.user.controller;

import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.UserOrderDetailResponse;
import com.shelfflow.services.common.dto.UserOrderCancelRequest;
import com.shelfflow.services.common.dto.UserOrderQuery;
import com.shelfflow.services.common.dto.UserOrderSubmitRequest;
import com.shelfflow.services.common.dto.UserOrderSubmitResponse;
import com.shelfflow.services.common.dto.UserOrderSummaryResponse;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.order.service.UserOrderApplicationService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/user/orders")
public class UserOrderController {

    private final UserOrderApplicationService userOrderApplicationService;

    public UserOrderController(UserOrderApplicationService userOrderApplicationService) {
        this.userOrderApplicationService = userOrderApplicationService;
    }

    @PostMapping
    public ApiResponse<UserOrderSubmitResponse> submit(UserAuthenticatedUser authenticatedUser,
                                                       @Valid @RequestBody UserOrderSubmitRequest request,
                                                       HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userOrderApplicationService.submit(authenticatedUser, request),
                requestId(servletRequest),
                "下单成功"
        );
    }

    @GetMapping
    public ApiResponse<PageResponse<UserOrderSummaryResponse>> page(UserAuthenticatedUser authenticatedUser,
                                                                    @Valid @ModelAttribute UserOrderQuery query,
                                                                    HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userOrderApplicationService.pageOrders(authenticatedUser, query),
                requestId(servletRequest),
                "查询成功"
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<UserOrderDetailResponse> detail(UserAuthenticatedUser authenticatedUser,
                                                       @PathVariable("id") String id,
                                                       HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userOrderApplicationService.getOrderDetail(authenticatedUser, id),
                requestId(servletRequest),
                "查询成功"
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancel(UserAuthenticatedUser authenticatedUser,
                                    @PathVariable("id") String id,
                                    @RequestBody(required = false) @Valid UserOrderCancelRequest request,
                                    HttpServletRequest servletRequest) {
        userOrderApplicationService.cancelOrder(authenticatedUser, id, request);
        return ApiResponse.success(null, requestId(servletRequest), "取消订单成功");
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<UserOrderDetailResponse> pay(UserAuthenticatedUser authenticatedUser,
                                                    @PathVariable("id") String id,
                                                    HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userOrderApplicationService.payOrder(authenticatedUser, id),
                requestId(servletRequest),
                "支付成功"
        );
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
