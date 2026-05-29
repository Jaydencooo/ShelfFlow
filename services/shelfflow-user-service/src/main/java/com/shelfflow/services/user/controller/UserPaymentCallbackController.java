package com.shelfflow.services.user.controller;

import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.dto.UserPaymentCallbackRequest;
import com.shelfflow.services.common.dto.UserPaymentCallbackResponse;
import com.shelfflow.services.user.order.service.UserOrderApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/user/payment-callbacks")
public class UserPaymentCallbackController {

    private static final String PAYMENT_SIGNATURE_HEADER = "X-ShelfFlow-Payment-Signature";

    private final UserOrderApplicationService userOrderApplicationService;

    public UserPaymentCallbackController(UserOrderApplicationService userOrderApplicationService) {
        this.userOrderApplicationService = userOrderApplicationService;
    }

    @PostMapping("/mock")
    public ApiResponse<UserPaymentCallbackResponse> mockCallback(
            @Valid @RequestBody UserPaymentCallbackRequest request,
            @RequestHeader(value = PAYMENT_SIGNATURE_HEADER, required = false) String signature,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(
                userOrderApplicationService.confirmPaymentCallback(request, signature),
                requestId(servletRequest),
                "支付回调处理成功"
        );
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
