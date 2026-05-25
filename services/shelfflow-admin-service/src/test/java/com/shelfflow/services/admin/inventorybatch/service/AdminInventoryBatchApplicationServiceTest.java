package com.shelfflow.services.admin.inventorybatch.service;

import com.shelfflow.services.admin.inventorybatch.domain.BatchInventoryPolicy;
import com.shelfflow.services.admin.inventorybatch.domain.BatchLifecyclePolicy;
import com.shelfflow.services.admin.inventorybatch.persistence.InventoryBatchPersistenceMapper;
import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchDataObject;
import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchPageCriteria;
import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchPageRow;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.api.SortOrder;
import com.shelfflow.services.common.domain.BatchStatus;
import com.shelfflow.services.common.domain.PricingStatus;
import com.shelfflow.services.common.dto.InventoryBatchQuery;
import com.shelfflow.services.common.dto.InventoryBatchRecordResponse;
import com.shelfflow.services.common.dto.InventoryBatchUpsertRequest;
import com.shelfflow.services.common.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInventoryBatchApplicationServiceTest {

    @Mock
    private InventoryBatchPersistenceMapper inventoryBatchPersistenceMapper;

    private AdminInventoryBatchApplicationService adminInventoryBatchApplicationService;

    private InventoryBatchUpsertRequest request;

    @BeforeEach
    void setUp() {
        adminInventoryBatchApplicationService = new AdminInventoryBatchApplicationService(
                inventoryBatchPersistenceMapper,
                new BatchLifecyclePolicy(),
                new BatchInventoryPolicy()
        );
        request = new InventoryBatchUpsertRequest();
        request.setProductId("46");
        request.setBatchCode("BATCH-001");
        request.setProductionDate("2026-05-12T00:00:00");
        request.setExpiryDate("2026-05-19T00:00:00");
        request.setStockQuantity(12);
        request.setBasePrice(new BigDecimal("9.90"));
        request.setBatchStatus(BatchStatus.ACTIVE);
        request.setPricingStatus(PricingStatus.ACTIVE);
    }

    @Test
    void pageShouldMapCriteriaAndResponse() {
        InventoryBatchQuery query = new InventoryBatchQuery();
        query.setPage(1);
        query.setPageSize(5);
        query.setKeyword("BATCH");
        query.setCategoryId("11");
        query.setBatchStatus(BatchStatus.ACTIVE);
        query.setPricingStatus(PricingStatus.ACTIVE);
        query.setSortBy("currentPrice");
        query.setSortOrder(SortOrder.ASC);

        InventoryBatchPageRow row = new InventoryBatchPageRow();
        row.setId(1L);
        row.setProductId(46L);
        row.setProductName("临期酸奶 200g");
        row.setCategoryId(11L);
        row.setBatchCode("SF-BATCH-001");
        row.setProductionTime(LocalDateTime.of(2026, 4, 1, 8, 0));
        row.setExpirationTime(LocalDateTime.of(2026, 4, 8, 23, 59));
        row.setAvailableQuantity(10);
        row.setLockedQuantity(1);
        row.setSoldQuantity(2);
        row.setWasteQuantity(0);
        row.setBasePrice(new BigDecimal("8.00"));
        row.setCurrentPrice(new BigDecimal("5.60"));
        row.setStatus(1);
        row.setDaysToExpire(6);

        when(inventoryBatchPersistenceMapper.page(any(InventoryBatchPageCriteria.class))).thenReturn(List.of(row));
        when(inventoryBatchPersistenceMapper.count(any(InventoryBatchPageCriteria.class))).thenReturn(1L);
        PageResponse<InventoryBatchRecordResponse> response = adminInventoryBatchApplicationService.page(query);

        ArgumentCaptor<InventoryBatchPageCriteria> criteriaCaptor = ArgumentCaptor.forClass(InventoryBatchPageCriteria.class);
        verify(inventoryBatchPersistenceMapper).page(criteriaCaptor.capture());
        assertEquals("currentPrice", criteriaCaptor.getValue().getSortColumn());
        assertEquals("ASC", criteriaCaptor.getValue().getSortDirection());
        assertEquals("active", criteriaCaptor.getValue().getPricingStatus());
        assertEquals("SF-BATCH-001", response.getItems().get(0).getBatchCode());
        assertEquals(PricingStatus.ACTIVE, response.getItems().get(0).getPricingStatus());
        assertEquals(1L, response.getTotal());
    }

    @Test
    void createShouldRejectDuplicateBatchCode() {
        when(inventoryBatchPersistenceMapper.existsProduct(46L)).thenReturn(true);
        when(inventoryBatchPersistenceMapper.findIdByBatchCode("BATCH-001")).thenReturn(9L);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> adminInventoryBatchApplicationService.create(1L, request)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
        verify(inventoryBatchPersistenceMapper, never()).insert(any());
    }

    @Test
    void updateShouldRejectStockLowerThanCommittedQuantity() {
        request.setStockQuantity(3);
        when(inventoryBatchPersistenceMapper.findDataById(1L)).thenReturn(existingBatch(1L, 2, 2));
        when(inventoryBatchPersistenceMapper.existsProduct(46L)).thenReturn(true);
        when(inventoryBatchPersistenceMapper.findIdByBatchCode("BATCH-001")).thenReturn(1L);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> adminInventoryBatchApplicationService.update(1L, "1", request)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
    }

    @Test
    void updateStatusShouldRejectManualTransitionFromExpired() {
        InventoryBatchDataObject existing = existingBatch(1L, 0, 0);
        existing.setStatus(3);
        when(inventoryBatchPersistenceMapper.findDataById(1L)).thenReturn(existing);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> adminInventoryBatchApplicationService.updateStatus(1L, 1L, BatchStatus.ACTIVE)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
        verify(inventoryBatchPersistenceMapper, never()).updateStatus(anyLong(), any(), anyLong(), any());
    }

    private InventoryBatchDataObject existingBatch(Long id, Integer lockedQuantity, Integer soldQuantity) {
        InventoryBatchDataObject dataObject = new InventoryBatchDataObject();
        dataObject.setId(id);
        dataObject.setProductId(46L);
        dataObject.setBatchCode("BATCH-001");
        dataObject.setProductionTime(LocalDateTime.of(2026, 5, 12, 0, 0));
        dataObject.setExpirationTime(LocalDateTime.of(2026, 5, 19, 0, 0));
        dataObject.setStockQuantity(12);
        dataObject.setLockedQuantity(lockedQuantity);
        dataObject.setSoldQuantity(soldQuantity);
        dataObject.setStatus(1);
        return dataObject;
    }
}
