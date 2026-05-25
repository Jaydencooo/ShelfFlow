package com.shelfflow.services.admin.order.service;

import com.shelfflow.services.admin.ShelfFlowAdminServiceApplication;
import com.shelfflow.services.admin.inventorybatch.persistence.InventoryBatchPersistenceMapper;
import com.shelfflow.services.admin.inventorybatch.persistence.dataobject.InventoryBatchDataObject;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.domain.UserOrderPayStatus;
import com.shelfflow.services.common.domain.UserOrderStatus;
import com.shelfflow.services.common.dto.AdminOrderDetailResponse;
import com.shelfflow.services.common.dto.AdminOrderQuery;
import com.shelfflow.services.common.dto.AdminOrderSummaryResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ShelfFlowAdminServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class AdminOrderFulfillmentApplicationServiceIntegrationTest {

    @Autowired
    private AdminOrderFulfillmentApplicationService adminOrderFulfillmentApplicationService;

    @Autowired
    private InventoryBatchPersistenceMapper inventoryBatchPersistenceMapper;

    @Test
    void pageShouldFilterPaidOrdersByStatus() {
        AdminOrderQuery query = new AdminOrderQuery();
        query.setStatus(UserOrderStatus.TO_PREPARE.value());
        query.setPayStatus(UserOrderPayStatus.PAID.value());

        PageResponse<AdminOrderSummaryResponse> response = adminOrderFulfillmentApplicationService.page(query);

        assertEquals(1L, response.getTotal());
        assertEquals("SFA202605190001", response.getItems().get(0).getOrderNumber());
        assertEquals(UserOrderStatus.TO_PREPARE, response.getItems().get(0).getStatus());
        assertEquals(2, response.getItems().get(0).getItemCount());
    }

    @Test
    void pageShouldSupportKeywordLookupByOrderId() {
        AdminOrderQuery query = new AdminOrderQuery();
        query.setKeyword("5001");

        PageResponse<AdminOrderSummaryResponse> response = adminOrderFulfillmentApplicationService.page(query);

        assertEquals(1L, response.getTotal());
        assertEquals("5001", response.getItems().get(0).getId());
        assertEquals("SFA202605190001", response.getItems().get(0).getOrderNumber());
    }

    @Test
    void getByIdShouldReturnOrderItems() {
        AdminOrderDetailResponse response = adminOrderFulfillmentApplicationService.getById("5001");

        assertEquals("SFA202605190001", response.getOrderNumber());
        assertEquals(UserOrderStatus.TO_PREPARE, response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getItemCount());
        assertEquals(2, response.getEvents().size());
    }

    @Test
    void updateStatusShouldAdvanceFulfillmentStep() {
        AdminOrderDetailResponse response = adminOrderFulfillmentApplicationService.updateStatus(
                99L,
                "5001",
                UserOrderStatus.PREPARING
        );

        assertEquals(UserOrderStatus.PREPARING, response.getStatus());
        assertEquals("fulfillment_updated", response.getEvents().get(response.getEvents().size() - 1).getEventType().value());
    }

    @Test
    void updateStatusShouldRejectUnpaidOrder() {
        assertThrows(ApplicationException.class,
                () -> adminOrderFulfillmentApplicationService.updateStatus(99L, "5003", UserOrderStatus.PREPARING));
    }

    @Test
    void completeOrderShouldSettleLockedStockToSoldStock() {
        InventoryBatchDataObject before = inventoryBatchPersistenceMapper.findDataById(2001L);

        AdminOrderDetailResponse response = adminOrderFulfillmentApplicationService.updateStatus(
                99L,
                "5002",
                UserOrderStatus.COMPLETED
        );

        InventoryBatchDataObject after = inventoryBatchPersistenceMapper.findDataById(2001L);
        assertEquals(UserOrderStatus.COMPLETED, response.getStatus());
        assertEquals(before.getLockedQuantity() - 1, after.getLockedQuantity());
        assertEquals(before.getSoldQuantity() + 1, after.getSoldQuantity());
        assertEquals("fulfillment_updated", response.getEvents().get(response.getEvents().size() - 1).getEventType().value());
    }

    @Test
    void updateStatusShouldRejectSkippedTransition() {
        assertThrows(ApplicationException.class,
                () -> adminOrderFulfillmentApplicationService.updateStatus(99L, "5001", UserOrderStatus.READY_FOR_PICKUP));
    }

    @Test
    void getByIdShouldRejectMissingOrder() {
        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> adminOrderFulfillmentApplicationService.getById("999999"));

        assertTrue(exception.getMessage().contains("订单不存在"));
    }
}
