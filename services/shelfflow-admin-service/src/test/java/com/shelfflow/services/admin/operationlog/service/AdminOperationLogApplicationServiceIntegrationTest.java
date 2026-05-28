package com.shelfflow.services.admin.operationlog.service;

import com.shelfflow.services.admin.ShelfFlowAdminServiceApplication;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminOperationLogQuery;
import com.shelfflow.services.common.dto.AdminOperationLogResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ShelfFlowAdminServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class AdminOperationLogApplicationServiceIntegrationTest {

    @Autowired
    private AdminOperationLogApplicationService operationLogApplicationService;

    @Test
    void latestShouldReturnNewestLogsFirst() {
        List<AdminOperationLogResponse> logs = operationLogApplicationService.latest(2);

        assertEquals(2, logs.size());
        assertEquals("INVENTORY_BATCH", logs.get(0).getModule());
        assertEquals("ORDER_FULFILLMENT", logs.get(1).getModule());
    }

    @Test
    void pageShouldFilterByModuleAndAction() {
        AdminOperationLogQuery query = new AdminOperationLogQuery();
        query.setModule("ORDER_FULFILLMENT");
        query.setAction("PICKUP_VERIFY");

        PageResponse<AdminOperationLogResponse> page = operationLogApplicationService.page(query);

        assertEquals(1L, page.getTotal());
        assertEquals("ORDER_FULFILLMENT PICKUP_VERIFY SUCCESS", page.getItems().get(0).getSummary());
        assertTrue(page.getItems().get(0).getPath().contains("pickup-verification"));
    }
}
