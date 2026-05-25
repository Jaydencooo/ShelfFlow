package com.shelfflow.services.admin.product.service;

import com.shelfflow.services.admin.product.domain.ProductCatalogPolicy;
import com.shelfflow.services.admin.product.persistence.ProductPersistenceMapper;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductPageCriteria;
import com.shelfflow.services.admin.product.persistence.dataobject.ProductPageRow;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.api.SortOrder;
import com.shelfflow.services.common.domain.ProductStatus;
import com.shelfflow.services.common.dto.ProductQuery;
import com.shelfflow.services.common.dto.ProductRecordResponse;
import com.shelfflow.services.common.dto.ProductUpsertRequest;
import com.shelfflow.services.common.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProductApplicationServiceTest {

    @Mock
    private ProductPersistenceMapper productPersistenceMapper;

    private AdminProductApplicationService adminProductApplicationService;

    private ProductUpsertRequest request;

    @BeforeEach
    void setUp() {
        adminProductApplicationService = new AdminProductApplicationService(
                productPersistenceMapper,
                new ProductCatalogPolicy()
        );
        request = new ProductUpsertRequest();
        request.setName("Test Product");
        request.setCategoryId("11");
        request.setPrice(new BigDecimal("9.90"));
        request.setDescription("desc");
        request.setImage("https://img");
        request.setStatus(ProductStatus.ACTIVE);
    }

    @Test
    void pageShouldMapRowsAndSanitizeSort() {
        ProductQuery query = new ProductQuery();
        query.setPage(2);
        query.setPageSize(10);
        query.setKeyword("milk");
        query.setCategoryId("11");
        query.setStatus(ProductStatus.ACTIVE);
        query.setSortBy("price");
        query.setSortOrder(SortOrder.ASC);

        ProductPageRow row = new ProductPageRow();
        row.setId(1L);
        row.setName("Fresh Milk");
        row.setCategoryId(11L);
        row.setCategoryName("临期乳品");
        row.setPrice(new BigDecimal("12.50"));
        row.setStatus(1);
        row.setDaysToExpire(3);

        when(productPersistenceMapper.page(any(ProductPageCriteria.class))).thenReturn(List.of(row));
        when(productPersistenceMapper.count(any(ProductPageCriteria.class))).thenReturn(1L);
        PageResponse<ProductRecordResponse> response = adminProductApplicationService.page(query);

        ArgumentCaptor<ProductPageCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductPageCriteria.class);
        verify(productPersistenceMapper).page(criteriaCaptor.capture());
        assertEquals("p.price", criteriaCaptor.getValue().getSortColumn());
        assertEquals("ASC", criteriaCaptor.getValue().getSortDirection());
        assertEquals(10, criteriaCaptor.getValue().getLimit());
        assertEquals(10, criteriaCaptor.getValue().getOffset());
        assertEquals("Fresh Milk", response.getItems().get(0).getName());
        assertEquals(ProductStatus.ACTIVE, response.getItems().get(0).getStatus());
    }

    @Test
    void createShouldRejectDuplicateName() {
        when(productPersistenceMapper.existsCategory(11L)).thenReturn(true);
        when(productPersistenceMapper.findIdByName("Test Product")).thenReturn(88L);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> adminProductApplicationService.create(1L, request)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
        verify(productPersistenceMapper, never()).insert(any());
    }

    @Test
    void updateShouldRejectMissingProduct() {
        when(productPersistenceMapper.findById(99L)).thenReturn(null);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> adminProductApplicationService.update(1L, "99", request)
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
    }
}
