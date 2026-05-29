package com.shelfflow.services.admin.inventorybatch.service;

import com.shelfflow.services.admin.cache.NoopStorefrontCatalogCacheInvalidator;
import com.shelfflow.services.admin.cache.StorefrontCatalogCacheInvalidator;
import com.shelfflow.services.admin.inventorybatch.domain.BatchInventoryPolicy;
import com.shelfflow.services.admin.inventorybatch.domain.BatchLifecyclePolicy;
import com.shelfflow.services.admin.inventorybatch.persistence.InventoryBatchPersistenceMapper;
import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchDataObject;
import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchPageCriteria;
import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchPageRow;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.domain.BatchStatus;
import com.shelfflow.services.common.domain.PricingStatus;
import com.shelfflow.services.common.dto.InventoryBatchQuery;
import com.shelfflow.services.common.dto.InventoryBatchRecordResponse;
import com.shelfflow.services.common.dto.InventoryBatchUpsertRequest;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AdminInventoryBatchApplicationService {

    private static final DateTimeFormatter RESPONSE_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String SORT_BY_UPDATED_AT = "updatedAt";
    private static final String SORT_BY_EXPIRY_DATE = "expiryDate";
    private static final String SORT_BY_PRODUCTION_DATE = "productionDate";
    private static final String SORT_BY_BATCH_CODE = "batchCode";
    private static final String SORT_BY_AVAILABLE_STOCK = "availableStock";
    private static final String SORT_BY_CURRENT_PRICE = "currentPrice";
    private static final String GENERATED_BATCH_CODE_PREFIX = "B";
    private static final String GENERATED_BATCH_PRODUCT_PREFIX = "P";
    private static final int GENERATED_BATCH_CODE_MAX_ATTEMPTS = 5;
    private static final int GENERATED_BATCH_RANDOM_LENGTH = 8;
    private static final DateTimeFormatter GENERATED_BATCH_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final InventoryBatchPersistenceMapper inventoryBatchPersistenceMapper;
    private final BatchLifecyclePolicy batchLifecyclePolicy;
    private final BatchInventoryPolicy batchInventoryPolicy;
    private final StorefrontCatalogCacheInvalidator storefrontCatalogCacheInvalidator;

    public AdminInventoryBatchApplicationService(InventoryBatchPersistenceMapper inventoryBatchPersistenceMapper,
                                                 BatchLifecyclePolicy batchLifecyclePolicy,
                                                 BatchInventoryPolicy batchInventoryPolicy) {
        this(inventoryBatchPersistenceMapper, batchLifecyclePolicy, batchInventoryPolicy, NoopStorefrontCatalogCacheInvalidator.INSTANCE);
    }

    @Autowired
    public AdminInventoryBatchApplicationService(InventoryBatchPersistenceMapper inventoryBatchPersistenceMapper,
                                                 BatchLifecyclePolicy batchLifecyclePolicy,
                                                 BatchInventoryPolicy batchInventoryPolicy,
                                                 ObjectProvider<StorefrontCatalogCacheInvalidator> storefrontCatalogCacheInvalidatorProvider) {
        this(inventoryBatchPersistenceMapper,
                batchLifecyclePolicy,
                batchInventoryPolicy,
                storefrontCatalogCacheInvalidatorProvider.getIfAvailable(() -> NoopStorefrontCatalogCacheInvalidator.INSTANCE));
    }

    private AdminInventoryBatchApplicationService(InventoryBatchPersistenceMapper inventoryBatchPersistenceMapper,
                                                  BatchLifecyclePolicy batchLifecyclePolicy,
                                                  BatchInventoryPolicy batchInventoryPolicy,
                                                  StorefrontCatalogCacheInvalidator storefrontCatalogCacheInvalidator) {
        this.inventoryBatchPersistenceMapper = inventoryBatchPersistenceMapper;
        this.batchLifecyclePolicy = batchLifecyclePolicy;
        this.batchInventoryPolicy = batchInventoryPolicy;
        this.storefrontCatalogCacheInvalidator = storefrontCatalogCacheInvalidator;
    }

    @Transactional
    public PageResponse<InventoryBatchRecordResponse> page(InventoryBatchQuery query) {
        refreshStatuses();
        InventoryBatchPageCriteria criteria = InventoryBatchPageCriteria.builder()
                .limit(query.getPageSize())
                .offset((query.getPage() - 1) * query.getPageSize())
                .keyword(blankToNull(query.getKeyword()))
                .categoryId(parseOptionalLong(query.getCategoryId(), "categoryId"))
                .status(query.getBatchStatus() == null ? null : query.getBatchStatus().legacyValue())
                .pricingStatus(query.getPricingStatus() == null ? null : query.getPricingStatus().value())
                .expiryDaysMin(query.getExpiryDaysMin())
                .expiryDaysMax(query.getExpiryDaysMax())
                .sortColumn(resolveSortColumn(query.getSortBy()))
                .sortDirection(query.getSortOrder().name())
                .build();

        List<InventoryBatchRecordResponse> items = inventoryBatchPersistenceMapper.page(criteria).stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<InventoryBatchRecordResponse>builder()
                .items(items)
                .total(inventoryBatchPersistenceMapper.count(criteria))
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }

    @Transactional
    public InventoryBatchRecordResponse getById(Long id) {
        refreshStatuses();
        InventoryBatchPageRow row = inventoryBatchPersistenceMapper.findById(id);
        if (row == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "批次不存在");
        }
        return toResponse(row);
    }

    @Transactional
    public void create(Long actorId, InventoryBatchUpsertRequest request) {
        Long productId = parseRequiredLong(request.getProductId(), "productId");
        ensureProductExists(productId);
        String batchCode = resolveCreateBatchCode(productId, request.getBatchCode());
        ensureBatchCodeUnique(batchCode, null);
        BatchStatus batchStatus = batchLifecyclePolicy.resolveWritableStatus(request.getBatchStatus());

        InventoryBatchDataObject batch = new InventoryBatchDataObject();
        batch.setProductId(productId);
        batch.setBatchCode(batchCode);
        batch.setProductionTime(parseDateTime(request.getProductionDate(), "productionDate"));
        batch.setExpirationTime(parseDateTime(request.getExpiryDate(), "expiryDate"));
        batchInventoryPolicy.validateDateRange(batch.getProductionTime(), batch.getExpirationTime());
        batch.setStockQuantity(request.getStockQuantity());
        batch.setLockedQuantity(BatchInventoryPolicy.INITIAL_LOCKED_STOCK);
        batch.setSoldQuantity(BatchInventoryPolicy.INITIAL_SOLD_STOCK);
        batch.setStatus(batchStatus.legacyValue());
        batch.setCreateTime(LocalDateTime.now());
        batch.setUpdateTime(batch.getCreateTime());
        batch.setCreateUser(actorId);
        batch.setUpdateUser(actorId);

        try {
            inventoryBatchPersistenceMapper.insert(batch);
        } catch (DuplicateKeyException exception) {
            throw new ApplicationException(ErrorCode.CONFLICT, "批次号已存在");
        }
        storefrontCatalogCacheInvalidator.invalidateCatalog();
    }

    @Transactional
    public void update(Long actorId, String id, InventoryBatchUpsertRequest request) {
        Long batchId = parseRequiredLong(id, "id");
        Long productId = parseRequiredLong(request.getProductId(), "productId");
        InventoryBatchDataObject existing = inventoryBatchPersistenceMapper.findDataById(batchId);
        if (existing == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "批次不存在");
        }

        ensureProductExists(productId);
        String batchCode = normalizeRequiredBatchCode(request.getBatchCode());
        ensureBatchCodeUnique(batchCode, batchId);
        BatchStatus batchStatus = batchLifecyclePolicy.resolveWritableStatus(request.getBatchStatus());
        batchInventoryPolicy.ensureStockNotBelowCommitted(request.getStockQuantity(), existing.getLockedQuantity(), existing.getSoldQuantity());

        existing.setProductId(productId);
        existing.setBatchCode(batchCode);
        existing.setProductionTime(parseDateTime(request.getProductionDate(), "productionDate"));
        existing.setExpirationTime(parseDateTime(request.getExpiryDate(), "expiryDate"));
        batchInventoryPolicy.validateDateRange(existing.getProductionTime(), existing.getExpirationTime());
        existing.setStockQuantity(request.getStockQuantity());
        existing.setStatus(batchStatus.legacyValue());
        existing.setUpdateTime(LocalDateTime.now());
        existing.setUpdateUser(actorId);

        try {
            inventoryBatchPersistenceMapper.update(existing);
        } catch (DuplicateKeyException exception) {
            throw new ApplicationException(ErrorCode.CONFLICT, "批次号已存在");
        }
        storefrontCatalogCacheInvalidator.invalidateCatalog();
    }

    @Transactional
    public void updateStatus(Long actorId, Long id, BatchStatus targetStatus) {
        InventoryBatchDataObject existing = inventoryBatchPersistenceMapper.findDataById(id);
        if (existing == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "批次不存在");
        }

        BatchStatus currentStatus = BatchStatus.fromLegacy(existing.getStatus());
        BatchStatus writableTarget = batchLifecyclePolicy.resolveWritableStatus(targetStatus);
        batchLifecyclePolicy.ensureManualTransitionAllowed(currentStatus, writableTarget);

        inventoryBatchPersistenceMapper.updateStatus(id, writableTarget.legacyValue(), actorId, LocalDateTime.now());
        storefrontCatalogCacheInvalidator.invalidateCatalog();
    }

    @Transactional
    public void delete(Long id) {
        InventoryBatchDataObject existing = inventoryBatchPersistenceMapper.findDataById(id);
        if (existing == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "批次不存在");
        }
        if (defaultZero(existing.getLockedQuantity()) > 0 || defaultZero(existing.getSoldQuantity()) > 0) {
            throw new ApplicationException(ErrorCode.CONFLICT, "批次已有锁定或已售库存，不能删除");
        }
        inventoryBatchPersistenceMapper.deleteById(id);
        storefrontCatalogCacheInvalidator.invalidateCatalog();
    }

    private void refreshStatuses() {
        int changedRows = inventoryBatchPersistenceMapper.restoreSaleableBatches()
                + inventoryBatchPersistenceMapper.markExpiredBatches()
                + inventoryBatchPersistenceMapper.markSoldOutBatches();
        if (changedRows > 0) {
            storefrontCatalogCacheInvalidator.invalidateCatalog();
        }
    }

    private InventoryBatchRecordResponse toResponse(InventoryBatchPageRow row) {
        return InventoryBatchRecordResponse.builder()
                .id(String.valueOf(row.getId()))
                .productId(String.valueOf(row.getProductId()))
                .productName(row.getProductName())
                .categoryId(row.getCategoryId() == null ? "" : String.valueOf(row.getCategoryId()))
                .batchCode(row.getBatchCode())
                .productionDate(formatDateTime(row.getProductionTime()))
                .expiryDate(formatDateTime(row.getExpirationTime()))
                .shelfLifeDays(row.getDaysToExpire() == null ? batchInventoryPolicy.calculateDaysUntilExpiry(row.getExpirationTime()) : row.getDaysToExpire())
                .availableStock(defaultZero(row.getAvailableQuantity()))
                .lockedStock(defaultZero(row.getLockedQuantity()))
                .soldStock(defaultZero(row.getSoldQuantity()))
                .wasteStock(defaultZero(row.getWasteQuantity()))
                .basePrice(row.getBasePrice())
                .currentPrice(row.getCurrentPrice())
                .batchStatus(BatchStatus.fromLegacy(row.getStatus()))
                .pricingStatus(batchLifecyclePolicy.resolvePricingStatus(row.getStatus()))
                .build();
    }

    private String resolveSortColumn(String sortBy) {
        String normalized = blankToNull(sortBy);
        if (normalized == null || SORT_BY_UPDATED_AT.equals(normalized)) {
            return "b.update_time";
        }
        if (SORT_BY_EXPIRY_DATE.equals(normalized)) {
            return "b.expiration_time";
        }
        if (SORT_BY_PRODUCTION_DATE.equals(normalized)) {
            return "b.production_time";
        }
        if (SORT_BY_BATCH_CODE.equals(normalized)) {
            return "b.batch_code";
        }
        if (SORT_BY_AVAILABLE_STOCK.equals(normalized)) {
            return "availableQuantity";
        }
        if (SORT_BY_CURRENT_PRICE.equals(normalized)) {
            return "currentPrice";
        }
        throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "sortBy 不支持");
    }

    private void ensureProductExists(Long productId) {
        if (!inventoryBatchPersistenceMapper.existsProduct(productId)) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "商品不存在");
        }
    }

    private void ensureBatchCodeUnique(String batchCode, Long currentId) {
        Long existingId = inventoryBatchPersistenceMapper.findIdByBatchCode(batchCode);
        if (existingId != null && !existingId.equals(currentId)) {
            throw new ApplicationException(ErrorCode.CONFLICT, "批次号已存在");
        }
    }

    private String resolveCreateBatchCode(Long productId, String requestedBatchCode) {
        String normalized = blankToNull(requestedBatchCode);
        if (normalized != null) {
            return normalized;
        }

        for (int attempt = 0; attempt < GENERATED_BATCH_CODE_MAX_ATTEMPTS; attempt++) {
            String generated = generateBatchCode(productId);
            if (inventoryBatchPersistenceMapper.findIdByBatchCode(generated) == null) {
                return generated;
            }
        }
        throw new ApplicationException(ErrorCode.CONFLICT, "批次号自动生成失败，请重试");
    }

    private String normalizeRequiredBatchCode(String batchCode) {
        String normalized = blankToNull(batchCode);
        if (normalized == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "batchCode 不能为空");
        }
        return normalized;
    }

    private String generateBatchCode(Long productId) {
        String dateSegment = LocalDateTime.now().format(GENERATED_BATCH_DATE_FORMATTER);
        String randomSegment = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, GENERATED_BATCH_RANDOM_LENGTH)
                .toUpperCase();
        return GENERATED_BATCH_CODE_PREFIX + "-"
                + GENERATED_BATCH_PRODUCT_PREFIX + productId + "-"
                + dateSegment + "-"
                + randomSegment;
    }

    private LocalDateTime parseDateTime(String value, String fieldName) {
        try {
            return LocalDateTime.parse(value);
        } catch (Exception exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 时间格式不合法");
        }
    }

    private Long parseRequiredLong(String value, String fieldName) {
        Long parsed = parseOptionalLong(value, fieldName);
        if (parsed == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 不能为空");
        }
        return parsed;
    }

    private Long parseOptionalLong(String value, String fieldName) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 必须为数字");
        }
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.format(RESPONSE_DATE_TIME_FORMATTER);
    }
}
