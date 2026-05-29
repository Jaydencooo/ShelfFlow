package com.shelfflow.services.user.config;

import com.shelfflow.services.common.cache.StorefrontCatalogCacheKeys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shelfflow.user.catalog.cache")
public class UserCatalogCacheProperties {
    private boolean enabled = false;
    private String redisKeyPrefix = StorefrontCatalogCacheKeys.DEFAULT_PREFIX;
    private long categoriesTtlSeconds = 120;
    private long productsTtlSeconds = 60;
    private boolean failOpen = true;
}
