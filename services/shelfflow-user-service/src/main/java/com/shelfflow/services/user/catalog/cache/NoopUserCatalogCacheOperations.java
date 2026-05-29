package com.shelfflow.services.user.catalog.cache;

import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.UserCatalogCategoryResponse;
import com.shelfflow.services.common.dto.UserCatalogProductResponse;

import java.util.List;
import java.util.Optional;

public final class NoopUserCatalogCacheOperations implements UserCatalogCacheOperations {

    public static final NoopUserCatalogCacheOperations INSTANCE = new NoopUserCatalogCacheOperations();

    private NoopUserCatalogCacheOperations() {
    }

    @Override
    public Optional<List<UserCatalogCategoryResponse>> getCategories() {
        return Optional.empty();
    }

    @Override
    public void putCategories(List<UserCatalogCategoryResponse> categories) {
    }

    @Override
    public Optional<PageResponse<UserCatalogProductResponse>> getProducts(UserCatalogCacheQuery query) {
        return Optional.empty();
    }

    @Override
    public void putProducts(UserCatalogCacheQuery query, PageResponse<UserCatalogProductResponse> response) {
    }
}
