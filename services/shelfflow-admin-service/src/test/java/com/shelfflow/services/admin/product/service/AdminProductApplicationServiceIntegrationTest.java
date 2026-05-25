package com.shelfflow.services.admin.product.service;

import com.shelfflow.services.admin.ShelfFlowAdminServiceApplication;
import com.shelfflow.services.admin.product.persistence.ProductPersistenceMapper;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductDataObject;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.api.SortOrder;
import com.shelfflow.services.common.domain.ProductStatus;
import com.shelfflow.services.common.dto.ProductQuery;
import com.shelfflow.services.common.dto.ProductRecordResponse;
import com.shelfflow.services.common.dto.ProductUpsertRequest;
import com.shelfflow.services.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = ShelfFlowAdminServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class AdminProductApplicationServiceIntegrationTest {

    @Autowired
    private AdminProductApplicationService adminProductApplicationService;

    @Autowired
    private ProductPersistenceMapper productPersistenceMapper;

    @Test
    void pageShouldQuerySeededProductsFromDatabase() {
        ProductQuery query = new ProductQuery();
        query.setCategoryId("11");
        query.setSortBy("price");
        query.setSortOrder(SortOrder.ASC);

        PageResponse<ProductRecordResponse> response = adminProductApplicationService.page(query);

        assertEquals(1L, response.getTotal());
        assertEquals("Fresh Milk", response.getItems().get(0).getName());
        assertEquals(ProductStatus.ACTIVE, response.getItems().get(0).getStatus());
    }

    @Test
    void createShouldPersistNormalizedProductIntoDatabase() {
        ProductUpsertRequest request = new ProductUpsertRequest();
        request.setName("  Yogurt Drink  ");
        request.setCategoryId("11");
        request.setPrice(new BigDecimal("8.80"));
        request.setImage("   ");
        request.setDescription("  发酵乳饮品  ");
        request.setStatus(ProductStatus.ACTIVE);

        adminProductApplicationService.create(99L, request);

        Long productId = productPersistenceMapper.findIdByName("Yogurt Drink");
        ProductDataObject saved = productPersistenceMapper.findById(productId);
        assertNotNull(saved);
        assertEquals("Yogurt Drink", saved.getName());
        assertEquals("发酵乳饮品", saved.getDescription());
        assertEquals(null, saved.getImage());
        assertEquals(99L, saved.getCreateUser());
    }

    @Test
    void updateShouldPersistChangedProductFields() {
        ProductUpsertRequest request = new ProductUpsertRequest();
        request.setName("Fresh Milk Plus");
        request.setCategoryId("11");
        request.setPrice(new BigDecimal("13.90"));
        request.setImage("https://img/milk-plus.png");
        request.setDescription("升级版鲜牛奶");
        request.setStatus(ProductStatus.INACTIVE);

        adminProductApplicationService.update(88L, "1001", request);

        ProductDataObject updated = productPersistenceMapper.findById(1001L);
        assertEquals("Fresh Milk Plus", updated.getName());
        assertEquals(new BigDecimal("13.90"), updated.getPrice());
        assertEquals(0, updated.getStatus());
        assertEquals(88L, updated.getUpdateUser());
    }

    @Test
    void createShouldRejectDuplicateProductNameFromDatabase() {
        ProductUpsertRequest request = new ProductUpsertRequest();
        request.setName("Fresh Milk");
        request.setCategoryId("11");
        request.setPrice(new BigDecimal("8.80"));
        request.setStatus(ProductStatus.ACTIVE);

        assertThrows(ApplicationException.class, () -> adminProductApplicationService.create(99L, request));
    }

    @Test
    void createShouldRejectMissingCategoryFromDatabase() {
        ProductUpsertRequest request = new ProductUpsertRequest();
        request.setName("Cold Brew Latte");
        request.setCategoryId("9999");
        request.setPrice(new BigDecimal("15.00"));
        request.setStatus(ProductStatus.ACTIVE);

        assertThrows(ApplicationException.class, () -> adminProductApplicationService.create(99L, request));
    }
}
