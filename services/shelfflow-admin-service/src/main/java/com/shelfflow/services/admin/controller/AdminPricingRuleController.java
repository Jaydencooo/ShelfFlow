package com.shelfflow.services.admin.controller;

import com.shelfflow.services.admin.pricing.service.AdminPricingRuleApplicationService;
import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminPricingRuleQuery;
import com.shelfflow.services.common.dto.AdminPricingRuleResponse;
import com.shelfflow.services.common.dto.AdminPricingRuleStatusUpdateRequest;
import com.shelfflow.services.common.dto.AdminPricingRuleUpsertRequest;
import com.shelfflow.services.common.dto.AdminPricingSuggestionResponse;
import com.shelfflow.services.common.security.AdminAuthenticatedUser;
import com.shelfflow.services.common.security.AdminPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
@RequestMapping("/api/admin/pricing-rules")
public class AdminPricingRuleController {

    private final AdminPricingRuleApplicationService pricingRuleApplicationService;

    public AdminPricingRuleController(AdminPricingRuleApplicationService pricingRuleApplicationService) {
        this.pricingRuleApplicationService = pricingRuleApplicationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminPricingRuleResponse>> page(AdminAuthenticatedUser authenticatedUser,
                                                                    @Valid @ModelAttribute AdminPricingRuleQuery query,
                                                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRICING_READ);
        return ApiResponse.success(pricingRuleApplicationService.page(query), requestId(request), "查询成功");
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminPricingRuleResponse> detail(AdminAuthenticatedUser authenticatedUser,
                                                        @PathVariable String id,
                                                        HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRICING_READ);
        return ApiResponse.success(pricingRuleApplicationService.getById(id), requestId(request), "查询成功");
    }

    @PostMapping
    public ApiResponse<AdminPricingRuleResponse> create(AdminAuthenticatedUser authenticatedUser,
                                                        @Valid @RequestBody AdminPricingRuleUpsertRequest requestBody,
                                                        HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRICING_WRITE);
        return ApiResponse.success(
                pricingRuleApplicationService.create(authenticatedUser.getUserId(), requestBody),
                requestId(request),
                "定价规则创建成功"
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminPricingRuleResponse> update(AdminAuthenticatedUser authenticatedUser,
                                                        @PathVariable String id,
                                                        @Valid @RequestBody AdminPricingRuleUpsertRequest requestBody,
                                                        HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRICING_WRITE);
        return ApiResponse.success(
                pricingRuleApplicationService.update(authenticatedUser.getUserId(), id, requestBody),
                requestId(request),
                "定价规则更新成功"
        );
    }

    @PostMapping("/{id}/status")
    public ApiResponse<AdminPricingRuleResponse> updateStatus(AdminAuthenticatedUser authenticatedUser,
                                                              @PathVariable String id,
                                                              @Valid @RequestBody AdminPricingRuleStatusUpdateRequest requestBody,
                                                              HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRICING_WRITE);
        return ApiResponse.success(
                pricingRuleApplicationService.updateStatus(authenticatedUser.getUserId(), id, requestBody.getStatus()),
                requestId(request),
                "定价规则状态更新成功"
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(AdminAuthenticatedUser authenticatedUser,
                                    @PathVariable String id,
                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRICING_WRITE);
        pricingRuleApplicationService.delete(id);
        return ApiResponse.success(null, requestId(request), "定价规则已删除");
    }

    @GetMapping("/suggestions")
    public ApiResponse<List<AdminPricingSuggestionResponse>> suggestions(AdminAuthenticatedUser authenticatedUser,
                                                                         HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRICING_READ);
        return ApiResponse.success(pricingRuleApplicationService.suggestions(), requestId(request), "查询成功");
    }

    @PostMapping("/suggestions/{batchId}/accept")
    public ApiResponse<AdminPricingRuleResponse> acceptSuggestion(AdminAuthenticatedUser authenticatedUser,
                                                                  @PathVariable String batchId,
                                                                  HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRICING_WRITE);
        return ApiResponse.success(
                pricingRuleApplicationService.acceptSuggestion(authenticatedUser.getUserId(), batchId),
                requestId(request),
                "AI 定价建议已采纳"
        );
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
