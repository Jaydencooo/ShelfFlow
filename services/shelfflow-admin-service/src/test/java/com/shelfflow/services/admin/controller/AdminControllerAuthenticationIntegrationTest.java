package com.shelfflow.services.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shelfflow.services.admin.inventorybatch.service.AdminInventoryBatchApplicationService;
import com.shelfflow.services.admin.order.service.AdminOrderFulfillmentApplicationService;
import com.shelfflow.services.admin.product.service.AdminProductApplicationService;
import com.shelfflow.services.common.dto.BatchStatusUpdateRequest;
import com.shelfflow.services.common.dto.AdminOrderStatusUpdateRequest;
import com.shelfflow.services.common.dto.InventoryBatchUpsertRequest;
import com.shelfflow.services.common.dto.ProductUpsertRequest;
import com.shelfflow.services.common.domain.UserOrderStatus;
import com.shelfflow.services.common.security.AdminAccessTokenParser;
import com.shelfflow.services.common.security.AdminAuthorizationProfile;
import com.shelfflow.services.common.security.AdminAuthenticatedUserArgumentResolver;
import com.shelfflow.services.common.security.AdminAuthenticationInterceptor;
import com.shelfflow.services.common.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerAuthenticationIntegrationTest {

    private static final long ADMIN_USER_ID = 42L;
    private static final String REQUEST_ID = "req-admin-001";

    @Mock
    private AdminProductApplicationService adminProductApplicationService;

    @Mock
    private AdminInventoryBatchApplicationService adminInventoryBatchApplicationService;

    @Mock
    private AdminOrderFulfillmentApplicationService adminOrderFulfillmentApplicationService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminProductController productController = new AdminProductController(adminProductApplicationService);
        AdminInventoryBatchController inventoryBatchController = new AdminInventoryBatchController(adminInventoryBatchApplicationService);
        AdminOrderController orderController = new AdminOrderController(adminOrderFulfillmentApplicationService);

        mockMvc = MockMvcBuilders.standaloneSetup(productController, inventoryBatchController, orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AdminAuthenticatedUserArgumentResolver(new AdminAuthorizationProfile()))
                .addInterceptors(new AdminAuthenticationInterceptor(new AdminAccessTokenParser(objectMapper)))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldRejectProductPageWhenAuthorizationHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/admin/products")
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"))
                .andExpect(jsonPath("$.requestId").value(REQUEST_ID));

        verifyNoInteractions(adminProductApplicationService);
    }

    @Test
    void shouldInjectAuthenticatedAdminForProductCreate() throws Exception {
        ProductUpsertRequest request = new ProductUpsertRequest();
        request.setName("酸奶");
        request.setCategoryId("11");
        request.setPrice(new BigDecimal("9.90"));

        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerToken(ADMIN_USER_ID))
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.requestId").value(REQUEST_ID));

        verify(adminProductApplicationService).create(ADMIN_USER_ID, request);
    }

    @Test
    void shouldInjectAuthenticatedAdminForProductUpdate() throws Exception {
        ProductUpsertRequest request = new ProductUpsertRequest();
        request.setName("酸奶");
        request.setCategoryId("11");
        request.setPrice(new BigDecimal("9.90"));

        mockMvc.perform(put("/api/admin/products/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerToken(ADMIN_USER_ID))
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"));

        verify(adminProductApplicationService).update(ADMIN_USER_ID, "100", request);
    }

    @Test
    void shouldInjectAuthenticatedAdminForBatchCreate() throws Exception {
        InventoryBatchUpsertRequest request = new InventoryBatchUpsertRequest();
        request.setProductId("79");
        request.setBatchCode("SF-BATCH-001");
        request.setProductionDate("2026-05-10T10:00:00");
        request.setExpiryDate("2026-05-20T10:00:00");
        request.setStockQuantity(12);
        request.setBasePrice(new BigDecimal("8.80"));

        mockMvc.perform(post("/api/admin/inventory-batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerToken(ADMIN_USER_ID))
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.requestId").value(REQUEST_ID));

        verify(adminInventoryBatchApplicationService).create(ADMIN_USER_ID, request);
    }

    @Test
    void shouldInjectAuthenticatedAdminForBatchStatusUpdate() throws Exception {
        BatchStatusUpdateRequest request = new BatchStatusUpdateRequest();
        request.setBatchStatus(com.shelfflow.services.common.domain.BatchStatus.PAUSED);

        mockMvc.perform(post("/api/admin/inventory-batches/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerToken(ADMIN_USER_ID))
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"));

        verify(adminInventoryBatchApplicationService).updateStatus(ADMIN_USER_ID, 3L, request.getBatchStatus());
    }

    @Test
    void shouldInjectAuthenticatedAdminForOrderStatusUpdate() throws Exception {
        AdminOrderStatusUpdateRequest request = new AdminOrderStatusUpdateRequest();
        request.setOrderStatus(UserOrderStatus.PREPARING);

        mockMvc.perform(post("/api/admin/orders/5001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerToken(ADMIN_USER_ID))
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"));

        verify(adminOrderFulfillmentApplicationService).updateStatus(ADMIN_USER_ID, "5001", request.getOrderStatus());
    }

    private String bearerToken(Long userId) throws Exception {
        String header = base64Url(objectMapper.writeValueAsBytes(Map.of("alg", "none", "typ", "JWT")));
        String payload = base64Url(objectMapper.writeValueAsBytes(Map.of("empId", userId)));
        return "Bearer " + header + "." + payload + ".signature";
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
