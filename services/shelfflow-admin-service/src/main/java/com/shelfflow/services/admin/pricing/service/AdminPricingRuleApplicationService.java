package com.shelfflow.services.admin.pricing.service;

import com.shelfflow.services.admin.cache.NoopStorefrontCatalogCacheInvalidator;
import com.shelfflow.services.admin.cache.StorefrontCatalogCacheInvalidator;
import com.shelfflow.services.admin.pricing.domain.AdminPricingRulePolicy;
import com.shelfflow.services.admin.pricing.persistence.AdminPricingRulePersistenceMapper;
import com.shelfflow.services.admin.pricing.persistence.dataobject.AdminPricingRuleCriteria;
import com.shelfflow.services.admin.pricing.persistence.dataobject.AdminPricingRuleDataObject;
import com.shelfflow.services.admin.pricing.persistence.dataobject.AdminPricingSuggestionRow;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.domain.PricingRuleStatus;
import com.shelfflow.services.common.dto.AdminPricingRuleQuery;
import com.shelfflow.services.common.dto.AdminPricingRuleResponse;
import com.shelfflow.services.common.dto.AdminPricingRuleUpsertRequest;
import com.shelfflow.services.common.dto.AdminPricingSuggestionResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminPricingRuleApplicationService {

    private static final String SORT_BY_UPDATED_AT = "updatedAt";
    private static final String SORT_BY_CREATED_AT = "createdAt";
    private static final String SORT_BY_PRIORITY = "priority";
    private static final String SORT_BY_DISCOUNT_RATE = "discountRate";
    private static final int DEFAULT_SUGGESTION_LIMIT = 20;
    private static final int ACCEPTED_SUGGESTION_PRIORITY = 100;
    private static final String AI_SUGGESTION_RULE_NAME_PREFIX = "AI 建议-";

    private final AdminPricingRulePersistenceMapper pricingRulePersistenceMapper;
    private final AdminPricingRulePolicy pricingRulePolicy;
    private final StorefrontCatalogCacheInvalidator storefrontCatalogCacheInvalidator;

    public AdminPricingRuleApplicationService(AdminPricingRulePersistenceMapper pricingRulePersistenceMapper,
                                              AdminPricingRulePolicy pricingRulePolicy) {
        this(pricingRulePersistenceMapper, pricingRulePolicy, NoopStorefrontCatalogCacheInvalidator.INSTANCE);
    }

    @Autowired
    public AdminPricingRuleApplicationService(AdminPricingRulePersistenceMapper pricingRulePersistenceMapper,
                                              AdminPricingRulePolicy pricingRulePolicy,
                                              ObjectProvider<StorefrontCatalogCacheInvalidator> storefrontCatalogCacheInvalidatorProvider) {
        this(pricingRulePersistenceMapper,
                pricingRulePolicy,
                storefrontCatalogCacheInvalidatorProvider.getIfAvailable(() -> NoopStorefrontCatalogCacheInvalidator.INSTANCE));
    }

    private AdminPricingRuleApplicationService(AdminPricingRulePersistenceMapper pricingRulePersistenceMapper,
                                               AdminPricingRulePolicy pricingRulePolicy,
                                               StorefrontCatalogCacheInvalidator storefrontCatalogCacheInvalidator) {
        this.pricingRulePersistenceMapper = pricingRulePersistenceMapper;
        this.pricingRulePolicy = pricingRulePolicy;
        this.storefrontCatalogCacheInvalidator = storefrontCatalogCacheInvalidator;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminPricingRuleResponse> page(AdminPricingRuleQuery query) {
        AdminPricingRuleCriteria criteria = AdminPricingRuleCriteria.builder()
                .limit(query.getPageSize())
                .offset((query.getPage() - 1) * query.getPageSize())
                .keyword(blankToNull(query.getKeyword()))
                .status(query.getStatus() == null ? null : query.getStatus().legacyValue())
                .sortColumn(resolveSortColumn(query.getSortBy()))
                .sortDirection(query.getSortOrder().name())
                .build();

        List<AdminPricingRuleResponse> items = pricingRulePersistenceMapper.page(criteria).stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<AdminPricingRuleResponse>builder()
                .items(items)
                .total(pricingRulePersistenceMapper.count(criteria))
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminPricingRuleResponse getById(String id) {
        AdminPricingRuleDataObject rule = pricingRulePersistenceMapper.findById(parseRequiredLong(id, "id"));
        if (rule == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "定价规则不存在");
        }
        return toResponse(rule);
    }

    @Transactional
    public AdminPricingRuleResponse create(Long actorId, AdminPricingRuleUpsertRequest request) {
        String normalizedName = pricingRulePolicy.normalizeName(request.getName());
        pricingRulePolicy.ensureValidDayRange(request.getMinDaysToExpire(), request.getMaxDaysToExpire());
        pricingRulePolicy.ensureUniqueName(pricingRulePersistenceMapper.findIdByName(normalizedName), null);

        LocalDateTime now = LocalDateTime.now();
        AdminPricingRuleDataObject rule = new AdminPricingRuleDataObject();
        rule.setName(normalizedName);
        rule.setMinDaysToExpire(request.getMinDaysToExpire());
        rule.setMaxDaysToExpire(request.getMaxDaysToExpire());
        rule.setDiscountRate(request.getDiscountRate());
        rule.setPriority(request.getPriority());
        rule.setStatus(resolveStatus(request.getStatus()).legacyValue());
        rule.setCreateTime(now);
        rule.setUpdateTime(now);
        rule.setCreateUser(actorId);
        rule.setUpdateUser(actorId);
        pricingRulePersistenceMapper.insert(rule);
        storefrontCatalogCacheInvalidator.invalidateCatalog();
        return toResponse(pricingRulePersistenceMapper.findById(rule.getId()));
    }

    @Transactional
    public AdminPricingRuleResponse update(Long actorId, String id, AdminPricingRuleUpsertRequest request) {
        Long ruleId = parseRequiredLong(id, "id");
        AdminPricingRuleDataObject existing = pricingRulePersistenceMapper.findById(ruleId);
        if (existing == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "定价规则不存在");
        }

        String normalizedName = pricingRulePolicy.normalizeName(request.getName());
        pricingRulePolicy.ensureValidDayRange(request.getMinDaysToExpire(), request.getMaxDaysToExpire());
        pricingRulePolicy.ensureUniqueName(pricingRulePersistenceMapper.findIdByName(normalizedName), ruleId);

        AdminPricingRuleDataObject rule = new AdminPricingRuleDataObject();
        rule.setId(ruleId);
        rule.setName(normalizedName);
        rule.setMinDaysToExpire(request.getMinDaysToExpire());
        rule.setMaxDaysToExpire(request.getMaxDaysToExpire());
        rule.setDiscountRate(request.getDiscountRate());
        rule.setPriority(request.getPriority());
        rule.setStatus(resolveStatus(request.getStatus()).legacyValue());
        rule.setUpdateTime(LocalDateTime.now());
        rule.setUpdateUser(actorId);
        pricingRulePersistenceMapper.update(rule);
        storefrontCatalogCacheInvalidator.invalidateCatalog();
        return toResponse(pricingRulePersistenceMapper.findById(ruleId));
    }

    @Transactional
    public AdminPricingRuleResponse updateStatus(Long actorId, String id, PricingRuleStatus status) {
        Long ruleId = parseRequiredLong(id, "id");
        if (pricingRulePersistenceMapper.findById(ruleId) == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "定价规则不存在");
        }
        pricingRulePersistenceMapper.updateStatus(ruleId, resolveStatus(status).legacyValue(), actorId);
        storefrontCatalogCacheInvalidator.invalidateCatalog();
        return toResponse(pricingRulePersistenceMapper.findById(ruleId));
    }

    @Transactional
    public void delete(String id) {
        Long ruleId = parseRequiredLong(id, "id");
        if (pricingRulePersistenceMapper.findById(ruleId) == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "定价规则不存在");
        }
        pricingRulePersistenceMapper.deleteById(ruleId);
        storefrontCatalogCacheInvalidator.invalidateCatalog();
    }

    @Transactional(readOnly = true)
    public List<AdminPricingSuggestionResponse> suggestions() {
        return pricingRulePersistenceMapper.listSuggestions(DEFAULT_SUGGESTION_LIMIT, AI_SUGGESTION_RULE_NAME_PREFIX).stream()
                .map(this::toSuggestion)
                .toList();
    }

    @Transactional
    public AdminPricingRuleResponse acceptSuggestion(Long actorId, String batchId) {
        AdminPricingSuggestionRow suggestion = pricingRulePersistenceMapper.listSuggestions(DEFAULT_SUGGESTION_LIMIT, AI_SUGGESTION_RULE_NAME_PREFIX).stream()
                .filter(row -> String.valueOf(row.getBatchId()).equals(batchId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND, "定价建议不存在或已失效"));

        String ruleName = AI_SUGGESTION_RULE_NAME_PREFIX + suggestion.getBatchCode();
        Long existingRuleId = pricingRulePersistenceMapper.findIdByName(ruleName);
        if (existingRuleId != null) {
            return toResponse(pricingRulePersistenceMapper.findById(existingRuleId));
        }

        BigDecimal discountRate = pricingRulePolicy.suggestDiscountRate(suggestion.getDaysToExpire());
        AdminPricingRuleUpsertRequest request = new AdminPricingRuleUpsertRequest();
        request.setName(ruleName);
        request.setMinDaysToExpire(Math.max(0, suggestion.getDaysToExpire() == null ? 0 : suggestion.getDaysToExpire()));
        request.setMaxDaysToExpire(Math.max(0, suggestion.getDaysToExpire() == null ? 0 : suggestion.getDaysToExpire()));
        request.setDiscountRate(discountRate);
        request.setPriority(ACCEPTED_SUGGESTION_PRIORITY);
        request.setStatus(PricingRuleStatus.ENABLED);
        return create(actorId, request);
    }

    private AdminPricingSuggestionResponse toSuggestion(AdminPricingSuggestionRow row) {
        BigDecimal discountRate = pricingRulePolicy.suggestDiscountRate(row.getDaysToExpire());
        return AdminPricingSuggestionResponse.builder()
                .id(String.valueOf(row.getBatchId()))
                .batchId(String.valueOf(row.getBatchId()))
                .batchCode(row.getBatchCode())
                .productId(String.valueOf(row.getProductId()))
                .productName(row.getProductName())
                .daysToExpire(row.getDaysToExpire())
                .availableStock(row.getAvailableStock())
                .currentPrice(row.getCurrentPrice())
                .suggestedDiscountRate(discountRate)
                .suggestedPrice(pricingRulePolicy.calculateSuggestedPrice(row.getCurrentPrice(), discountRate))
                .confidence(pricingRulePolicy.confidence(row.getDaysToExpire()))
                .reason("该批次剩余 " + row.getDaysToExpire() + " 天过期，建议按临期折扣策略加速流转")
                .build();
    }

    private AdminPricingRuleResponse toResponse(AdminPricingRuleDataObject rule) {
        return AdminPricingRuleResponse.builder()
                .id(String.valueOf(rule.getId()))
                .name(rule.getName())
                .minDaysToExpire(rule.getMinDaysToExpire())
                .maxDaysToExpire(rule.getMaxDaysToExpire())
                .discountRate(rule.getDiscountRate())
                .priority(rule.getPriority())
                .status(PricingRuleStatus.fromLegacy(rule.getStatus()))
                .createTime(rule.getCreateTime())
                .updateTime(rule.getUpdateTime())
                .build();
    }

    private PricingRuleStatus resolveStatus(PricingRuleStatus status) {
        return status == null ? PricingRuleStatus.ENABLED : status;
    }

    private Long parseRequiredLong(String value, String fieldName) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 不能为空");
        }
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 不是有效数字");
        }
    }

    private String resolveSortColumn(String sortBy) {
        String normalized = blankToNull(sortBy);
        if (normalized == null || SORT_BY_UPDATED_AT.equals(normalized)) {
            return "update_time";
        }
        if (SORT_BY_CREATED_AT.equals(normalized)) {
            return "create_time";
        }
        if (SORT_BY_PRIORITY.equals(normalized)) {
            return "priority";
        }
        if (SORT_BY_DISCOUNT_RATE.equals(normalized)) {
            return "discount_rate";
        }
        throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "sortBy 不支持");
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
