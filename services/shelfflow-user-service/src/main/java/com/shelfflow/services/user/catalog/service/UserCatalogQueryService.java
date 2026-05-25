package com.shelfflow.services.user.catalog.service;

import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.UserCatalogCategoryResponse;
import com.shelfflow.services.common.dto.UserCatalogProductDetailResponse;
import com.shelfflow.services.common.dto.UserCatalogProductQuery;
import com.shelfflow.services.common.dto.UserCatalogProductResponse;
import com.shelfflow.services.user.catalog.domain.UserCatalogPolicy;
import com.shelfflow.services.user.catalog.persistence.UserCatalogPersistenceMapper;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogPricingRuleRow;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogProductCriteria;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogProductDetailRow;
import com.shelfflow.services.user.catalog.persistence.dataobject.UserCatalogProductRow;
import com.shelfflow.services.user.catalog.support.UserCatalogSpecParser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserCatalogQueryService {

    private final UserCatalogPersistenceMapper userCatalogPersistenceMapper;
    private final UserCatalogPolicy userCatalogPolicy;
    private final UserCatalogSpecParser userCatalogSpecParser;

    public UserCatalogQueryService(UserCatalogPersistenceMapper userCatalogPersistenceMapper,
                                   UserCatalogPolicy userCatalogPolicy,
                                   UserCatalogSpecParser userCatalogSpecParser) {
        this.userCatalogPersistenceMapper = userCatalogPersistenceMapper;
        this.userCatalogPolicy = userCatalogPolicy;
        this.userCatalogSpecParser = userCatalogSpecParser;
    }

    public List<UserCatalogCategoryResponse> listProductCategories() {
        return userCatalogPersistenceMapper.listActiveCategories(UserCatalogPolicy.PRODUCT_CATEGORY_TYPE).stream()
                .map(category -> UserCatalogCategoryResponse.builder()
                        .id(String.valueOf(category.getId()))
                        .name(category.getName())
                        .sort(category.getSort())
                        .build())
                .toList();
    }

    public PageResponse<UserCatalogProductResponse> pageProducts(UserCatalogProductQuery query) {
        UserCatalogProductCriteria criteria = UserCatalogProductCriteria.builder()
                .keyword(userCatalogPolicy.normalizeKeyword(query.getKeyword()))
                .categoryId(userCatalogPolicy.parseOptionalCategoryId(query.getCategoryId()))
                .sortColumn(userCatalogPolicy.resolveSortColumn(query.getSortBy()))
                .sortDirection(query.getSortOrder().name())
                .limit(query.getPageSize())
                .offset((query.getPage() - 1) * query.getPageSize())
                .build();

        List<UserCatalogPricingRuleRow> pricingRules = userCatalogPersistenceMapper.listEnabledPricingRules();
        List<UserCatalogProductResponse> items = userCatalogPersistenceMapper.pageActiveProducts(criteria).stream()
                .map(product -> toResponse(product, pricingRules))
                .toList();

        return PageResponse.<UserCatalogProductResponse>builder()
                .items(items)
                .total(userCatalogPersistenceMapper.countActiveProducts(criteria))
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }

    public UserCatalogProductDetailResponse getProductDetail(String productId) {
        Long resolvedProductId = userCatalogPolicy.parseRequiredProductId(productId);
        UserCatalogProductDetailRow product = userCatalogPersistenceMapper.findSellableProductDetailById(resolvedProductId);
        userCatalogPolicy.ensureSellableProductExists(product != null);

        List<UserCatalogPricingRuleRow> pricingRules = userCatalogPersistenceMapper.listEnabledPricingRules();
        Integer daysToExpire = userCatalogPolicy.calculateDaysToExpire(product.getNearestExpirationTime());
        BigDecimal discountRate = resolveDiscountRate(daysToExpire, pricingRules);
        return UserCatalogProductDetailResponse.builder()
                .id(String.valueOf(product.getId()))
                .name(product.getName())
                .categoryId(String.valueOf(product.getCategoryId()))
                .categoryName(product.getCategoryName())
                .image(product.getImage())
                .description(product.getDescription())
                .listPrice(product.getPrice())
                .currentPrice(userCatalogPolicy.calculateCurrentPrice(product.getPrice(), daysToExpire, discountRate))
                .recommendedBatchId(product.getRecommendedBatchId() == null ? null : String.valueOf(product.getRecommendedBatchId()))
                .nearestExpiryDate(userCatalogPolicy.formatExpiryDate(product.getNearestExpirationTime()))
                .daysToExpire(daysToExpire)
                .availableQuantity(product.getAvailableQuantity())
                .specs(userCatalogPersistenceMapper.listProductSpecs(resolvedProductId).stream()
                        .map(userCatalogSpecParser::toResponse)
                        .toList())
                .build();
    }

    private UserCatalogProductResponse toResponse(UserCatalogProductRow product, List<UserCatalogPricingRuleRow> pricingRules) {
        Integer daysToExpire = userCatalogPolicy.calculateDaysToExpire(product.getNearestExpirationTime());
        BigDecimal discountRate = resolveDiscountRate(daysToExpire, pricingRules);
        return UserCatalogProductResponse.builder()
                .id(String.valueOf(product.getId()))
                .name(product.getName())
                .categoryId(String.valueOf(product.getCategoryId()))
                .categoryName(product.getCategoryName())
                .image(product.getImage())
                .description(product.getDescription())
                .listPrice(product.getPrice())
                .currentPrice(userCatalogPolicy.calculateCurrentPrice(product.getPrice(), daysToExpire, discountRate))
                .recommendedBatchId(product.getRecommendedBatchId() == null ? null : String.valueOf(product.getRecommendedBatchId()))
                .nearestExpiryDate(userCatalogPolicy.formatExpiryDate(product.getNearestExpirationTime()))
                .daysToExpire(daysToExpire)
                .availableQuantity(product.getAvailableQuantity())
                .build();
    }

    private BigDecimal resolveDiscountRate(Integer daysToExpire, List<UserCatalogPricingRuleRow> pricingRules) {
        if (daysToExpire == null) {
            return BigDecimal.ONE;
        }
        for (UserCatalogPricingRuleRow pricingRule : pricingRules) {
            if (pricingRule.getMinDaysToExpire() != null
                    && pricingRule.getMaxDaysToExpire() != null
                    && daysToExpire >= pricingRule.getMinDaysToExpire()
                    && daysToExpire <= pricingRule.getMaxDaysToExpire()) {
                return pricingRule.getDiscountRate();
            }
        }
        return BigDecimal.ONE;
    }
}
