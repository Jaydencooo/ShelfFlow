package com.shelfflow.services.user.catalog.cache;

import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.UserCatalogCategoryResponse;
import com.shelfflow.services.common.dto.UserCatalogProductResponse;

import java.util.List;
import java.util.Optional;

public interface UserCatalogCacheOperations {

    Optional<List<UserCatalogCategoryResponse>> getCategories();

    void putCategories(List<UserCatalogCategoryResponse> categories);

    Optional<PageResponse<UserCatalogProductResponse>> getProducts(UserCatalogCacheQuery query);

    void putProducts(UserCatalogCacheQuery query, PageResponse<UserCatalogProductResponse> response);
}
