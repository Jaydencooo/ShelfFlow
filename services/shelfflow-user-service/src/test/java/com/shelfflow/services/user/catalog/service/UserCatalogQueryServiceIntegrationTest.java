package com.shelfflow.services.user.catalog.service;

import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.api.SortOrder;
import com.shelfflow.services.common.dto.UserCatalogCategoryResponse;
import com.shelfflow.services.common.dto.UserCatalogProductDetailResponse;
import com.shelfflow.services.common.dto.UserCatalogProductQuery;
import com.shelfflow.services.common.dto.UserCatalogProductResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.user.ShelfFlowUserServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = ShelfFlowUserServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class UserCatalogQueryServiceIntegrationTest {

    @Autowired
    private UserCatalogQueryService userCatalogQueryService;

    @Test
    void listProductCategoriesShouldOnlyReturnActiveProductCategories() {
        List<UserCatalogCategoryResponse> categories = userCatalogQueryService.listProductCategories();

        assertEquals(2, categories.size());
        assertEquals("11", categories.get(0).getId());
        assertEquals("12", categories.get(1).getId());
    }

    @Test
    void pageProductsShouldReturnOnlySellableProductsWithDynamicPrices() {
        UserCatalogProductQuery query = new UserCatalogProductQuery();
        query.setSortBy("price");
        query.setSortOrder(SortOrder.ASC);

        PageResponse<UserCatalogProductResponse> response = userCatalogQueryService.pageProducts(query);

        assertEquals(2L, response.getTotal());
        assertEquals("Baguette", response.getItems().get(0).getName());
        assertEquals(new BigDecimal("4.95"), response.getItems().get(0).getCurrentPrice());
        assertEquals("5", response.getItems().get(0).getAvailableQuantity().toString());
        assertEquals("Fresh Milk", response.getItems().get(1).getName());
        assertEquals(new BigDecimal("8.75"), response.getItems().get(1).getCurrentPrice());
    }

    @Test
    void pageProductsShouldSupportCategoryFilter() {
        UserCatalogProductQuery query = new UserCatalogProductQuery();
        query.setCategoryId("11");

        PageResponse<UserCatalogProductResponse> response = userCatalogQueryService.pageProducts(query);

        assertEquals(1L, response.getTotal());
        assertEquals("Fresh Milk", response.getItems().get(0).getName());
    }

    @Test
    void pageProductsShouldRejectInvalidCategoryId() {
        UserCatalogProductQuery query = new UserCatalogProductQuery();
        query.setCategoryId("abc");

        assertThrows(ApplicationException.class, () -> userCatalogQueryService.pageProducts(query));
    }

    @Test
    void getProductDetailShouldReturnSellableProductWithSpecs() {
        UserCatalogProductDetailResponse detail = userCatalogQueryService.getProductDetail("1001");

        assertEquals("Fresh Milk", detail.getName());
        assertEquals("11", detail.getCategoryId());
        assertEquals(new BigDecimal("8.75"), detail.getCurrentPrice());
        assertEquals("2001", detail.getRecommendedBatchId());
        assertEquals(2, detail.getSpecs().size());
        Map<String, List<String>> specMap = detail.getSpecs().stream()
                .collect(Collectors.toMap(spec -> spec.getName(), spec -> spec.getValues()));
        assertEquals(List.of("chilled"), specMap.get("storageTemp"));
        assertEquals(List.of("250ml", "1L"), specMap.get("size"));
    }

    @Test
    void getProductDetailShouldRejectUnsellableProduct() {
        assertThrows(ApplicationException.class, () -> userCatalogQueryService.getProductDetail("1004"));
    }

    @Test
    void getProductDetailShouldRejectInvalidProductId() {
        assertThrows(ApplicationException.class, () -> userCatalogQueryService.getProductDetail("milk"));
    }
}
