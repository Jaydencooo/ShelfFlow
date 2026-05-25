package com.shelfflow.services.admin.inventorybatch.service;

import com.shelfflow.services.admin.ShelfFlowAdminServiceApplication;
import com.shelfflow.services.admin.inventorybatch.persistence.InventoryBatchPersistenceMapper;
import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchDataObject;
import com.shelfflow.services.common.domain.BatchStatus;
import com.shelfflow.services.common.domain.PricingStatus;
import com.shelfflow.services.common.dto.InventoryBatchQuery;
import com.shelfflow.services.common.dto.InventoryBatchRecordResponse;
import com.shelfflow.services.common.dto.InventoryBatchUpsertRequest;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ShelfFlowAdminServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class AdminInventoryBatchApplicationServiceIntegrationTest {

    @Autowired
    private AdminInventoryBatchApplicationService adminInventoryBatchApplicationService;

    @Autowired
    private InventoryBatchPersistenceMapper inventoryBatchPersistenceMapper;

    @Test
    void getByIdShouldReturnComputedBatchFieldsFromDatabase() {
        InventoryBatchRecordResponse response = adminInventoryBatchApplicationService.getById(2001L);

        assertEquals("BATCH-1001-A", response.getBatchCode());
        assertEquals("Fresh Milk", response.getProductName());
        assertEquals(BatchStatus.ACTIVE, response.getBatchStatus());
        assertEquals(PricingStatus.ACTIVE, response.getPricingStatus());
        assertTrue(response.getAvailableStock() > 0);
        assertEquals(new BigDecimal("8.75"), response.getCurrentPrice());
    }

    @Test
    void createShouldPersistBatchIntoDatabase() {
        InventoryBatchUpsertRequest request = new InventoryBatchUpsertRequest();
        request.setProductId("1001");
        request.setBatchCode("BATCH-1001-B");
        request.setProductionDate("2026-05-13T10:00:00");
        request.setExpiryDate("2026-05-20T10:00:00");
        request.setStockQuantity(18);
        request.setBasePrice(new BigDecimal("12.50"));
        request.setBatchStatus(BatchStatus.ACTIVE);
        request.setPricingStatus(PricingStatus.ACTIVE);

        adminInventoryBatchApplicationService.create(77L, request);

        Long batchId = inventoryBatchPersistenceMapper.findIdByBatchCode("BATCH-1001-B");
        InventoryBatchDataObject saved = inventoryBatchPersistenceMapper.findDataById(batchId);
        assertNotNull(saved);
        assertEquals(1001L, saved.getProductId());
        assertEquals(18, saved.getStockQuantity());
        assertEquals(77L, saved.getCreateUser());
        assertEquals(0, saved.getLockedQuantity());
        assertEquals(0, saved.getSoldQuantity());
    }

    @Test
    void updateStatusShouldPersistPausedState() {
        adminInventoryBatchApplicationService.updateStatus(66L, 2001L, BatchStatus.PAUSED);

        InventoryBatchDataObject updated = inventoryBatchPersistenceMapper.findDataById(2001L);
        assertEquals(0, updated.getStatus());
        assertEquals(66L, updated.getUpdateUser());
    }

    @Test
    void updateStatusShouldRejectTransitionFromExpiredBatch() {
        InventoryBatchDataObject expired = inventoryBatchPersistenceMapper.findDataById(2002L);
        expired.setStatus(3);
        inventoryBatchPersistenceMapper.updateStatus(2002L, 3, 1L, expired.getUpdateTime());

        assertThrows(ApplicationException.class,
                () -> adminInventoryBatchApplicationService.updateStatus(88L, 2002L, BatchStatus.ACTIVE));
    }

    @Test
    void createShouldRejectDuplicateBatchCodeFromDatabase() {
        InventoryBatchUpsertRequest request = new InventoryBatchUpsertRequest();
        request.setProductId("1001");
        request.setBatchCode("BATCH-1001-A");
        request.setProductionDate("2026-05-13T10:00:00");
        request.setExpiryDate("2026-05-20T10:00:00");
        request.setStockQuantity(18);
        request.setBasePrice(new BigDecimal("12.50"));
        request.setBatchStatus(BatchStatus.ACTIVE);
        request.setPricingStatus(PricingStatus.ACTIVE);

        assertThrows(ApplicationException.class,
                () -> adminInventoryBatchApplicationService.create(77L, request));
    }

    @Test
    void updateShouldRejectMissingProductFromDatabase() {
        InventoryBatchUpsertRequest request = new InventoryBatchUpsertRequest();
        request.setProductId("9999");
        request.setBatchCode("BATCH-1001-A");
        request.setProductionDate("2026-05-13T10:00:00");
        request.setExpiryDate("2026-05-20T10:00:00");
        request.setStockQuantity(18);
        request.setBasePrice(new BigDecimal("12.50"));
        request.setBatchStatus(BatchStatus.ACTIVE);
        request.setPricingStatus(PricingStatus.ACTIVE);

        assertThrows(ApplicationException.class,
                () -> adminInventoryBatchApplicationService.update(77L, "2001", request));
    }
}
