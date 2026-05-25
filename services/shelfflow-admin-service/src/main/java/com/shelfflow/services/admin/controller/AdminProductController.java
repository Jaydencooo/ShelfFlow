package com.shelfflow.services.admin.controller;

import com.shelfflow.services.admin.product.service.AdminProductApplicationService;
import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminProductCategoryResponse;
import com.shelfflow.services.common.dto.AdminProductCategoryUpsertRequest;
import com.shelfflow.services.common.dto.ProductQuery;
import com.shelfflow.services.common.dto.ProductRecordResponse;
import com.shelfflow.services.common.dto.ProductUpsertRequest;
import com.shelfflow.services.common.security.AdminPermission;
import com.shelfflow.services.common.security.AdminAuthenticatedUser;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductApplicationService adminProductApplicationService;

    public AdminProductController(AdminProductApplicationService adminProductApplicationService) {
        this.adminProductApplicationService = adminProductApplicationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductRecordResponse>> page(AdminAuthenticatedUser authenticatedUser,
                                                                 ProductQuery query,
                                                                 HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRODUCT_READ);
        return ApiResponse.success(adminProductApplicationService.page(query), requestId(request), "查询成功");
    }

    @GetMapping("/categories")
    public ApiResponse<List<AdminProductCategoryResponse>> categories(AdminAuthenticatedUser authenticatedUser,
                                                                      HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRODUCT_READ);
        return ApiResponse.success(adminProductApplicationService.listActiveProductCategories(), requestId(request), "查询成功");
    }

    @PostMapping("/categories")
    public ApiResponse<AdminProductCategoryResponse> createCategory(AdminAuthenticatedUser authenticatedUser,
                                                                    @Valid @RequestBody AdminProductCategoryUpsertRequest requestBody,
                                                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRODUCT_WRITE);
        return ApiResponse.success(
                adminProductApplicationService.createCategory(authenticatedUser.getUserId(), requestBody),
                requestId(request),
                "分类创建成功"
        );
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(AdminAuthenticatedUser authenticatedUser,
                                            @PathVariable String id,
                                            HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRODUCT_WRITE);
        adminProductApplicationService.deleteCategory(authenticatedUser.getUserId(), id);
        return ApiResponse.success(null, requestId(request), "分类已删除");
    }

    @PostMapping
    public ApiResponse<Void> create(AdminAuthenticatedUser authenticatedUser,
                                    @Valid @RequestBody ProductUpsertRequest requestBody,
                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRODUCT_WRITE);
        adminProductApplicationService.create(authenticatedUser.getUserId(), requestBody);
        return ApiResponse.success(null, requestId(request), "商品创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(AdminAuthenticatedUser authenticatedUser,
                                    @PathVariable String id,
                                    @Valid @RequestBody ProductUpsertRequest requestBody,
                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRODUCT_WRITE);
        adminProductApplicationService.update(authenticatedUser.getUserId(), id, requestBody);
        return ApiResponse.success(null, requestId(request), "商品更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(AdminAuthenticatedUser authenticatedUser,
                                    @PathVariable String id,
                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.PRODUCT_WRITE);
        adminProductApplicationService.delete(authenticatedUser.getUserId(), id);
        return ApiResponse.success(null, requestId(request), "商品已删除");
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
