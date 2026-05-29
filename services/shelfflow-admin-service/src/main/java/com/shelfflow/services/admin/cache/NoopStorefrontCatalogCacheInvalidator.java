package com.shelfflow.services.admin.cache;

public final class NoopStorefrontCatalogCacheInvalidator extends StorefrontCatalogCacheInvalidator {

    public static final NoopStorefrontCatalogCacheInvalidator INSTANCE = new NoopStorefrontCatalogCacheInvalidator();

    private NoopStorefrontCatalogCacheInvalidator() {
    }

    @Override
    public void invalidateCatalog() {
    }
}
