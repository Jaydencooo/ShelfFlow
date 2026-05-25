package com.shelfflow.services.user.controller;

import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.UserCatalogCategoryResponse;
import com.shelfflow.services.common.dto.UserCatalogProductDetailResponse;
import com.shelfflow.services.common.dto.UserCatalogProductQuery;
import com.shelfflow.services.common.dto.UserCatalogProductResponse;
import com.shelfflow.services.user.catalog.service.UserCatalogQueryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/user/catalog")
public class UserCatalogController {

    private final UserCatalogQueryService userCatalogQueryService;

    public UserCatalogController(UserCatalogQueryService userCatalogQueryService) {
        this.userCatalogQueryService = userCatalogQueryService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<UserCatalogCategoryResponse>> categories(HttpServletRequest request) {
        return ApiResponse.success(
                userCatalogQueryService.listProductCategories(),
                requestId(request),
                "查询成功"
        );
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<UserCatalogProductResponse>> products(@Valid @ModelAttribute UserCatalogProductQuery query,
                                                                          HttpServletRequest request) {
        return ApiResponse.success(
                userCatalogQueryService.pageProducts(query),
                requestId(request),
                "查询成功"
        );
    }

    @GetMapping("/products/{id}")
    public ApiResponse<UserCatalogProductDetailResponse> productDetail(@PathVariable("id") String id,
                                                                       HttpServletRequest request) {
        return ApiResponse.success(
                userCatalogQueryService.getProductDetail(id),
                requestId(request),
                "查询成功"
        );
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
