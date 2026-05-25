package com.shelfflow.services.admin.product.service;

import com.shelfflow.services.admin.product.domain.ProductCatalogPolicy;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductCategoryDataObject;
import com.shelfflow.services.admin.product.persistence.ProductPersistenceMapper;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductDataObject;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductPageCriteria;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductPageRow;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.domain.ProductStatus;
import com.shelfflow.services.common.dto.AdminProductCategoryResponse;
import com.shelfflow.services.common.dto.AdminProductCategoryUpsertRequest;
import com.shelfflow.services.common.dto.ProductQuery;
import com.shelfflow.services.common.dto.ProductRecordResponse;
import com.shelfflow.services.common.dto.ProductUpsertRequest;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminProductApplicationService {

    private static final String SORT_BY_UPDATED_AT = "updatedAt";
    private static final String SORT_BY_CREATED_AT = "createdAt";
    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_PRICE = "price";
    private static final String SORT_BY_STATUS = "status";
    private static final int PRODUCT_CATEGORY_TYPE = 1;
    private static final int ENABLED_STATUS = 1;
    private static final int DELETED_PRODUCT_STATUS = -1;
    private static final int CATEGORY_SORT_STEP = 10;

    private final ProductPersistenceMapper productPersistenceMapper;
    private final ProductCatalogPolicy productCatalogPolicy;

    public AdminProductApplicationService(ProductPersistenceMapper productPersistenceMapper,
                                          ProductCatalogPolicy productCatalogPolicy) {
        this.productPersistenceMapper = productPersistenceMapper;
        this.productCatalogPolicy = productCatalogPolicy;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductRecordResponse> page(ProductQuery query) {
        ProductPageCriteria criteria = ProductPageCriteria.builder()
                .limit(query.getPageSize())
                .offset((query.getPage() - 1) * query.getPageSize())
                .keyword(blankToNull(query.getKeyword()))
                .categoryId(parseOptionalLong(query.getCategoryId(), "categoryId"))
                .status(query.getStatus() == null ? null : query.getStatus().legacyValue())
                .sortColumn(resolveSortColumn(query.getSortBy()))
                .sortDirection(query.getSortOrder().name())
                .build();

        List<ProductRecordResponse> items = productPersistenceMapper.page(criteria).stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<ProductRecordResponse>builder()
                .items(items)
                .total(productPersistenceMapper.count(criteria))
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminProductCategoryResponse> listActiveProductCategories() {
        return productPersistenceMapper.listActiveProductCategories().stream()
                .map(row -> AdminProductCategoryResponse.builder()
                        .id(String.valueOf(row.getId()))
                        .name(row.getName())
                        .sort(row.getSort())
                        .productCount(row.getProductCount() == null ? 0 : row.getProductCount())
                        .build())
                .toList();
    }

    @Transactional
    public AdminProductCategoryResponse createCategory(Long actorId, AdminProductCategoryUpsertRequest request) {
        String normalizedName = productCatalogPolicy.normalizeCategoryName(request.getName());
        productCatalogPolicy.ensureUniqueCategoryName(productPersistenceMapper.findCategoryIdByName(normalizedName));

        LocalDateTime now = LocalDateTime.now();
        ProductCategoryDataObject category = new ProductCategoryDataObject();
        category.setType(PRODUCT_CATEGORY_TYPE);
        category.setName(normalizedName);
        category.setSort(resolveNextCategorySort());
        category.setStatus(ENABLED_STATUS);
        category.setCreateTime(now);
        category.setUpdateTime(now);
        category.setCreateUser(actorId);
        category.setUpdateUser(actorId);
        productPersistenceMapper.insertCategory(category);

        ProductCategoryDataObject created = productPersistenceMapper.findCategoryById(category.getId());
        return AdminProductCategoryResponse.builder()
                .id(String.valueOf(created.getId()))
                .name(created.getName())
                .sort(created.getSort())
                .productCount(0)
                .build();
    }

    @Transactional
    public void create(Long actorId, ProductUpsertRequest request) {
        Long categoryId = parseRequiredLong(request.getCategoryId(), "categoryId");
        String normalizedName = productCatalogPolicy.normalizeName(request.getName());
        ProductStatus writableStatus = productCatalogPolicy.resolveWritableStatus(request.getStatus());
        ensureCategoryExists(categoryId);
        ensureUniqueName(normalizedName, null);

        LocalDateTime now = LocalDateTime.now();
        ProductDataObject product = new ProductDataObject();
        product.setName(normalizedName);
        product.setCategoryId(categoryId);
        product.setPrice(request.getPrice());
        product.setImage(productCatalogPolicy.normalizeOptionalText(request.getImage()));
        product.setDescription(productCatalogPolicy.normalizeOptionalText(request.getDescription()));
        product.setStatus(writableStatus.legacyValue());
        product.setCreateTime(now);
        product.setUpdateTime(now);
        product.setCreateUser(actorId);
        product.setUpdateUser(actorId);
        productPersistenceMapper.insert(product);
    }

    @Transactional
    public void update(Long actorId, String id, ProductUpsertRequest request) {
        Long productId = parseRequiredLong(id, "id");
        Long categoryId = parseRequiredLong(request.getCategoryId(), "categoryId");
        String normalizedName = productCatalogPolicy.normalizeName(request.getName());
        ProductStatus writableStatus = productCatalogPolicy.resolveWritableStatus(request.getStatus());
        ProductDataObject existing = productPersistenceMapper.findById(productId);
        if (existing == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "商品不存在");
        }

        ensureCategoryExists(categoryId);
        ensureUniqueName(normalizedName, productId);

        ProductDataObject product = new ProductDataObject();
        product.setId(productId);
        product.setName(normalizedName);
        product.setCategoryId(categoryId);
        product.setPrice(request.getPrice());
        product.setImage(productCatalogPolicy.normalizeOptionalText(request.getImage()));
        product.setDescription(productCatalogPolicy.normalizeOptionalText(request.getDescription()));
        product.setStatus(writableStatus.legacyValue());
        product.setUpdateTime(LocalDateTime.now());
        product.setUpdateUser(actorId);
        productPersistenceMapper.update(product);

        if (writableStatus == ProductStatus.INACTIVE) {
            productPersistenceMapper.pauseActiveBatchesByProduct(productId, actorId);
        }
    }

    @Transactional
    public void delete(Long actorId, String id) {
        Long productId = parseRequiredLong(id, "id");
        ProductDataObject existing = productPersistenceMapper.findById(productId);
        if (existing == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "商品不存在");
        }

        productPersistenceMapper.pauseActiveBatchesByProduct(productId, actorId);
        productPersistenceMapper.logicallyDeleteProduct(productId, DELETED_PRODUCT_STATUS, actorId);
    }

    @Transactional
    public void deleteCategory(Long actorId, String id) {
        Long categoryId = parseRequiredLong(id, "id");
        ProductCategoryDataObject category = productPersistenceMapper.findCategoryById(categoryId);
        if (category == null || category.getType() == null || category.getType() != PRODUCT_CATEGORY_TYPE || category.getStatus() == null || category.getStatus() != ENABLED_STATUS) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "商品分类不存在");
        }

        productCatalogPolicy.ensureCategoryCanBeDeleted(productPersistenceMapper.countProductsByCategory(categoryId));
        productPersistenceMapper.disableCategory(categoryId, actorId);
    }

    private void ensureCategoryExists(Long categoryId) {
        productCatalogPolicy.ensureCategoryExists(productPersistenceMapper.existsCategory(categoryId));
    }

    private void ensureUniqueName(String name, Long currentProductId) {
        Long existingId = productPersistenceMapper.findIdByName(name);
        productCatalogPolicy.ensureUniqueName(existingId, currentProductId);
    }

    private int resolveNextCategorySort() {
        Integer currentMaxSort = productPersistenceMapper.findMaxCategorySort();
        return (currentMaxSort == null ? 0 : currentMaxSort) + CATEGORY_SORT_STEP;
    }

    private ProductRecordResponse toResponse(ProductPageRow row) {
        return ProductRecordResponse.builder()
                .id(String.valueOf(row.getId()))
                .name(row.getName())
                .categoryId(String.valueOf(row.getCategoryId()))
                .categoryName(row.getCategoryName())
                .price(row.getPrice())
                .image(row.getImage())
                .description(row.getDescription())
                .status(ProductStatus.fromLegacy(row.getStatus()))
                .shelfLifeDays(row.getDaysToExpire())
                .build();
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
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 不是有效数字");
        }
    }

    private String resolveSortColumn(String sortBy) {
        String normalized = blankToNull(sortBy);
        if (normalized == null || SORT_BY_UPDATED_AT.equals(normalized)) {
            return "p.update_time";
        }
        if (SORT_BY_CREATED_AT.equals(normalized)) {
            return "p.create_time";
        }
        if (SORT_BY_NAME.equals(normalized)) {
            return "p.name";
        }
        if (SORT_BY_PRICE.equals(normalized)) {
            return "p.price";
        }
        if (SORT_BY_STATUS.equals(normalized)) {
            return "p.status";
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
