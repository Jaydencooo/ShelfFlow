package com.shelfflow.services.admin.config;

import com.shelfflow.services.common.cache.StorefrontCatalogCacheKeys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shelfflow.admin.storefront-cache")
public class AdminStorefrontCacheProperties {
    private boolean invalidationEnabled = false;
    private String redisKeyPrefix = StorefrontCatalogCacheKeys.DEFAULT_PREFIX;
    private long scanCount = 200;
    private boolean failOpen = true;
}
