package com.shelfflow.services.user.order.service;

import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.user.config.UserOrderProperties;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderCartItemRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class UserInventoryReservationService {

    private static final Long LUA_SUCCESS = 1L;
    private static final String REDIS_KEY_SEPARATOR = ":";
    private static final String RESERVED_KEY_SUFFIX = "reserved";
    private static final long MIN_TTL_SECONDS = 1L;

    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
            """
            local key = KEYS[1]
            local quantity = tonumber(ARGV[1])
            local available = tonumber(ARGV[2])
            local ttl = tonumber(ARGV[3])

            if quantity == nil or available == nil or ttl == nil then
                return 0
            end
            if quantity <= 0 or available < quantity then
                return 0
            end

            local reserved = tonumber(redis.call('get', key) or '0')
            if reserved + quantity > available then
                return 0
            end

            redis.call('incrby', key, quantity)
            if ttl > 0 then
                redis.call('expire', key, ttl)
            end
            return 1
            """,
            Long.class
    );

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            """
            local key = KEYS[1]
            local quantity = tonumber(ARGV[1])
            local ttl = tonumber(ARGV[2])

            if quantity == nil or quantity <= 0 then
                return tonumber(redis.call('get', key) or '0')
            end

            local reserved = tonumber(redis.call('get', key) or '0')
            local next = reserved - quantity
            if next <= 0 then
                redis.call('del', key)
                return 0
            end

            redis.call('set', key, next)
            if ttl ~= nil and ttl > 0 then
                redis.call('expire', key, ttl)
            end
            return next
            """,
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final UserOrderProperties userOrderProperties;

    public UserInventoryReservationService(StringRedisTemplate stringRedisTemplate,
                                           UserOrderProperties userOrderProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userOrderProperties = userOrderProperties;
    }

    public List<ReservedInventoryItem> reserve(List<UserOrderCartItemRow> cartItems) {
        if (!isRedisLuaEnabled() || cartItems == null || cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReservedInventoryItem> reservedItems = new ArrayList<>();
        try {
            for (UserOrderCartItemRow cartItem : cartItems) {
                ReservedInventoryItem reservedItem = reserveOne(cartItem);
                reservedItems.add(reservedItem);
            }
            return reservedItems;
        } catch (RuntimeException ex) {
            release(reservedItems);
            throw ex;
        }
    }

    public void release(List<ReservedInventoryItem> reservedItems) {
        if (!isRedisLuaEnabled() || reservedItems == null || reservedItems.isEmpty()) {
            return;
        }

        for (ReservedInventoryItem reservedItem : reservedItems) {
            try {
                stringRedisTemplate.execute(
                        RELEASE_SCRIPT,
                        Collections.singletonList(reservedItem.redisKey()),
                        String.valueOf(reservedItem.quantity()),
                        String.valueOf(resolveReservationTtlSeconds())
                );
            } catch (RedisSystemException | IllegalStateException ex) {
                handleRedisFailure("release", reservedItem.batchId(), ex);
            }
        }
    }

    private ReservedInventoryItem reserveOne(UserOrderCartItemRow cartItem) {
        Integer quantity = cartItem.getQuantity();
        Integer availableQuantity = cartItem.getAvailableQuantity();
        if (cartItem.getBatchId() == null || quantity == null || availableQuantity == null) {
            throw new ApplicationException(ErrorCode.CONFLICT, "库存批次不可售，请刷新后重试");
        }

        String redisKey = buildRedisKey(cartItem.getBatchId());
        Long result;
        try {
            result = stringRedisTemplate.execute(
                    RESERVE_SCRIPT,
                    Collections.singletonList(redisKey),
                    String.valueOf(quantity),
                    String.valueOf(availableQuantity),
                    String.valueOf(resolveReservationTtlSeconds())
            );
        } catch (RedisSystemException | IllegalStateException ex) {
            handleRedisFailure("reserve", cartItem.getBatchId(), ex);
            return new ReservedInventoryItem(cartItem.getBatchId(), redisKey, 0);
        }

        if (!LUA_SUCCESS.equals(result)) {
            throw new ApplicationException(ErrorCode.CONFLICT, "库存不足，请刷新后重试");
        }
        return new ReservedInventoryItem(cartItem.getBatchId(), redisKey, quantity);
    }

    private boolean isRedisLuaEnabled() {
        UserOrderProperties.InventoryReservation reservation = userOrderProperties.getInventoryReservation();
        return reservation != null
                && reservation.getMode() == UserOrderProperties.InventoryReservationMode.REDIS_LUA;
    }

    private String buildRedisKey(Long batchId) {
        String prefix = userOrderProperties.getInventoryReservation().getRedisKeyPrefix();
        String normalizedPrefix = StringUtils.hasText(prefix)
                ? prefix.trim()
                : "shelfflow:inventory:batch";
        return normalizedPrefix + REDIS_KEY_SEPARATOR + batchId + REDIS_KEY_SEPARATOR + RESERVED_KEY_SUFFIX;
    }

    private long resolveReservationTtlSeconds() {
        long configuredTtl = userOrderProperties.getInventoryReservation().getReservationTtlSeconds();
        return Math.max(configuredTtl, MIN_TTL_SECONDS);
    }

    private void handleRedisFailure(String operation, Long batchId, RuntimeException ex) {
        if (userOrderProperties.getInventoryReservation().isFailOpen()) {
            log.warn("Inventory reservation Redis {} failed, fallback to database guard. batchId={}", operation, batchId, ex);
            return;
        }
        log.error("Inventory reservation Redis {} failed. batchId={}", operation, batchId, ex);
        throw new ApplicationException(ErrorCode.DEPENDENCY_ERROR, "库存预占服务暂时不可用，请稍后重试");
    }

    public record ReservedInventoryItem(Long batchId, String redisKey, int quantity) {
    }
}
