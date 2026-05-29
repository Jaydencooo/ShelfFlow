package com.shelfflow.services.user.catalog.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.cache.StorefrontCatalogCacheKeys;
import com.shelfflow.services.common.dto.UserCatalogCategoryResponse;
import com.shelfflow.services.common.dto.UserCatalogProductResponse;
import com.shelfflow.services.user.config.UserCatalogCacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "shelfflow.user.catalog.cache", name = "enabled", havingValue = "true")
public class RedisUserCatalogCacheService implements UserCatalogCacheOperations {

    private static final Logger log = LoggerFactory.getLogger(RedisUserCatalogCacheService.class);
    private static final String FIELD_ITEMS = "items";
    private static final String FIELD_TOTAL = "total";
    private static final String FIELD_PAGE = "page";
    private static final String FIELD_PAGE_SIZE = "pageSize";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final UserCatalogCacheProperties properties;

    public RedisUserCatalogCacheService(StringRedisTemplate stringRedisTemplate,
                                        ObjectMapper objectMapper,
                                        UserCatalogCacheProperties properties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public Optional<List<UserCatalogCategoryResponse>> getCategories() {
        try {
            String payload = stringRedisTemplate.opsForValue().get(categoriesKey());
            if (payload == null || payload.isBlank()) {
                return Optional.empty();
            }
            ArrayNode array = (ArrayNode) objectMapper.readTree(payload);
            List<UserCatalogCategoryResponse> categories = new ArrayList<>(array.size());
            for (JsonNode node : array) {
                categories.add(UserCatalogCategoryResponse.builder()
                        .id(text(node, "id"))
                        .name(text(node, "name"))
                        .sort(integer(node, "sort"))
                        .build());
            }
            return Optional.of(categories);
        } catch (Exception exception) {
            handleCacheFailure("读取用户目录分类缓存失败", exception);
            return Optional.empty();
        }
    }

    @Override
    public void putCategories(List<UserCatalogCategoryResponse> categories) {
        try {
            stringRedisTemplate.opsForValue().set(
                    categoriesKey(),
                    objectMapper.writeValueAsString(categories),
                    Duration.ofSeconds(properties.getCategoriesTtlSeconds())
            );
        } catch (Exception exception) {
            handleCacheFailure("写入用户目录分类缓存失败", exception);
        }
    }

    @Override
    public Optional<PageResponse<UserCatalogProductResponse>> getProducts(UserCatalogCacheQuery query) {
        try {
            String payload = stringRedisTemplate.opsForValue().get(productsKey(query));
            if (payload == null || payload.isBlank()) {
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(payload);
            List<UserCatalogProductResponse> items = new ArrayList<>();
            for (JsonNode node : root.path(FIELD_ITEMS)) {
                items.add(toProduct(node));
            }
            return Optional.of(PageResponse.<UserCatalogProductResponse>builder()
                    .items(items)
                    .total(root.path(FIELD_TOTAL).asLong())
                    .page(root.path(FIELD_PAGE).asInt(query.getPage()))
                    .pageSize(root.path(FIELD_PAGE_SIZE).asInt(query.getPageSize()))
                    .build());
        } catch (Exception exception) {
            handleCacheFailure("读取用户目录商品缓存失败", exception);
            return Optional.empty();
        }
    }

    @Override
    public void putProducts(UserCatalogCacheQuery query, PageResponse<UserCatalogProductResponse> response) {
        try {
            stringRedisTemplate.opsForValue().set(
                    productsKey(query),
                    toJson(response),
                    Duration.ofSeconds(properties.getProductsTtlSeconds())
            );
        } catch (Exception exception) {
            handleCacheFailure("写入用户目录商品缓存失败", exception);
        }
    }

    private String toJson(PageResponse<UserCatalogProductResponse> response) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put(FIELD_TOTAL, response.getTotal());
        root.put(FIELD_PAGE, response.getPage());
        root.put(FIELD_PAGE_SIZE, response.getPageSize());
        ArrayNode items = root.putArray(FIELD_ITEMS);
        for (UserCatalogProductResponse product : response.getItems()) {
            ObjectNode node = items.addObject();
            node.put("id", product.getId());
            node.put("name", product.getName());
            node.put("categoryId", product.getCategoryId());
            node.put("categoryName", product.getCategoryName());
            node.put("image", product.getImage());
            node.put("description", product.getDescription());
            putDecimal(node, "listPrice", product.getListPrice());
            putDecimal(node, "currentPrice", product.getCurrentPrice());
            node.put("recommendedBatchId", product.getRecommendedBatchId());
            node.put("nearestExpiryDate", product.getNearestExpiryDate());
            putInteger(node, "daysToExpire", product.getDaysToExpire());
            putInteger(node, "availableQuantity", product.getAvailableQuantity());
        }
        return objectMapper.writeValueAsString(root);
    }

    private UserCatalogProductResponse toProduct(JsonNode node) {
        return UserCatalogProductResponse.builder()
                .id(text(node, "id"))
                .name(text(node, "name"))
                .categoryId(text(node, "categoryId"))
                .categoryName(text(node, "categoryName"))
                .image(text(node, "image"))
                .description(text(node, "description"))
                .listPrice(decimal(node, "listPrice"))
                .currentPrice(decimal(node, "currentPrice"))
                .recommendedBatchId(text(node, "recommendedBatchId"))
                .nearestExpiryDate(text(node, "nearestExpiryDate"))
                .daysToExpire(integer(node, "daysToExpire"))
                .availableQuantity(integer(node, "availableQuantity"))
                .build();
    }

    private void putDecimal(ObjectNode node, String fieldName, BigDecimal value) {
        if (value == null) {
            node.putNull(fieldName);
            return;
        }
        node.put(fieldName, value);
    }

    private void putInteger(ObjectNode node, String fieldName, Integer value) {
        if (value == null) {
            node.putNull(fieldName);
            return;
        }
        node.put(fieldName, value);
    }

    private String categoriesKey() {
        return StorefrontCatalogCacheKeys.categoriesKey(properties.getRedisKeyPrefix());
    }

    private String productsKey(UserCatalogCacheQuery query) {
        return StorefrontCatalogCacheKeys.productsKey(
                properties.getRedisKeyPrefix(),
                query.getKeyword(),
                query.getCategoryId(),
                query.getSortColumn(),
                query.getSortDirection(),
                query.getPage(),
                query.getPageSize()
        );
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Integer integer(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asInt();
    }

    private BigDecimal decimal(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.decimalValue();
    }

    private void handleCacheFailure(String message, Exception exception) {
        if (!properties.isFailOpen()) {
            throw new IllegalStateException(message, exception);
        }
        log.warn("{}，已降级为数据库读取: {}", message, exception.getMessage());
    }
}
