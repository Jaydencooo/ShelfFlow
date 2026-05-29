package com.shelfflow.services.admin.cache;

import com.shelfflow.services.admin.config.AdminStorefrontCacheProperties;
import com.shelfflow.services.common.cache.StorefrontCatalogCacheKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "shelfflow.admin.storefront-cache", name = "invalidation-enabled", havingValue = "true")
public class RedisStorefrontCatalogCacheInvalidator extends StorefrontCatalogCacheInvalidator {

    private static final Logger log = LoggerFactory.getLogger(RedisStorefrontCatalogCacheInvalidator.class);
    private static final int DELETE_BATCH_SIZE = 100;

    private final RedisConnectionFactory redisConnectionFactory;
    private final AdminStorefrontCacheProperties properties;

    public RedisStorefrontCatalogCacheInvalidator(RedisConnectionFactory redisConnectionFactory,
                                                  AdminStorefrontCacheProperties properties) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.properties = properties;
    }

    @Override
    public void invalidateCatalog() {
        String pattern = StorefrontCatalogCacheKeys.allKeysPattern(properties.getRedisKeyPrefix());
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(pattern)
                .count(properties.getScanCount())
                .build();
        try (RedisConnection connection = redisConnectionFactory.getConnection();
             Cursor<byte[]> cursor = connection.scan(scanOptions)) {
            List<byte[]> keys = new ArrayList<>(DELETE_BATCH_SIZE);
            while (cursor.hasNext()) {
                keys.add(cursor.next());
                if (keys.size() >= DELETE_BATCH_SIZE) {
                    deleteBatch(connection, keys);
                }
            }
            deleteBatch(connection, keys);
        } catch (Exception exception) {
            if (!properties.isFailOpen()) {
                throw new IllegalStateException("清理用户端目录缓存失败", exception);
            }
            log.warn("清理用户端目录缓存失败，已忽略本次失效: {}", exception.getMessage());
        }
    }

    private void deleteBatch(RedisConnection connection, List<byte[]> keys) {
        if (keys.isEmpty()) {
            return;
        }
        connection.del(keys.toArray(new byte[0][]));
        if (log.isDebugEnabled()) {
            log.debug("已清理用户端目录缓存 {} 个 key", keys.size());
        }
        keys.clear();
    }
}
