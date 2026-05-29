package com.shelfflow.services.user.order.service;

import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.UserCartItemAddRequest;
import com.shelfflow.services.common.dto.UserCartItemResponse;
import com.shelfflow.services.common.dto.UserOrderCancelRequest;
import com.shelfflow.services.common.dto.UserOrderQuery;
import com.shelfflow.services.common.dto.UserOrderDetailResponse;
import com.shelfflow.services.common.dto.UserOrderSubmitRequest;
import com.shelfflow.services.common.dto.UserOrderSubmitResponse;
import com.shelfflow.services.common.dto.UserOrderSummaryResponse;
import com.shelfflow.services.common.dto.UserPaymentCallbackRequest;
import com.shelfflow.services.common.dto.UserPaymentCallbackResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.ShelfFlowUserServiceApplication;
import com.shelfflow.services.user.cart.service.UserCartApplicationService;
import com.shelfflow.services.user.order.persistence.UserOrderPersistenceMapper;
import com.shelfflow.services.user.order.persistence.dataobject.UserOrderPaymentDataObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = ShelfFlowUserServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class UserOrderApplicationServiceIntegrationTest {

    private static final UserAuthenticatedUser USER = new UserAuthenticatedUser(4001L, "openid-seeded", "token");
    private static final UserAuthenticatedUser EMAIL_ONLY_USER = new UserAuthenticatedUser(4002L, "recover@example.com", "token");
    private static final String PAYMENT_CALLBACK_SECRET = "test-payment-callback-secret";

    @Autowired
    private UserOrderApplicationService userOrderApplicationService;

    @Autowired
    private UserCartApplicationService userCartApplicationService;

    @Autowired
    private UserOrderPersistenceMapper userOrderPersistenceMapper;

    @Autowired
    private UserOrderTimeoutCloseService userOrderTimeoutCloseService;

    @Test
    void submitShouldCreateOrderLockInventoryAndClearCart() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        request.setQuantity(2);
        userCartApplicationService.addItem(USER, request);

        UserOrderSubmitRequest submitRequest = new UserOrderSubmitRequest();
        submitRequest.setRemark("尽快备货");

        UserOrderSubmitResponse response = userOrderApplicationService.submit(USER, submitRequest);

        assertNotNull(response.getId());
        assertNotNull(response.getOrderNumber());
        assertEquals(2, response.getItemCount());
        assertEquals(new BigDecimal("17.50"), response.getTotalAmount());
        assertEquals(0, userCartApplicationService.listItems(USER).size());

        UserOrderQuery query = new UserOrderQuery();
        query.setPage(1);
        query.setPageSize(10);
        PageResponse<UserOrderSummaryResponse> page = userOrderApplicationService.pageOrders(USER, query);
        assertFalse(page.getItems().isEmpty());
        assertEquals("尽快备货", page.getItems().get(0).getRemark());

        UserOrderDetailResponse detail = userOrderApplicationService.getOrderDetail(USER, response.getId());
        assertEquals("Seed Contact", detail.getConsignee());
        assertEquals("13800008888", detail.getPhone());
    }

    @Test
    void submitShouldUseSelectedPickupContact() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        request.setQuantity(1);
        userCartApplicationService.addItem(USER, request);

        UserOrderSubmitRequest submitRequest = new UserOrderSubmitRequest();
        submitRequest.setPickupContactId("8001");
        UserOrderSubmitResponse response = userOrderApplicationService.submit(USER, submitRequest);

        UserOrderDetailResponse detail = userOrderApplicationService.getOrderDetail(USER, response.getId());
        assertEquals("Seed Contact", detail.getConsignee());
        assertEquals("13800008888", detail.getPhone());
    }

    @Test
    void submitShouldOnlyUseSelectedCartItems() {
        UserCartItemAddRequest milkRequest = new UserCartItemAddRequest();
        milkRequest.setProductId("1001");
        milkRequest.setQuantity(1);
        userCartApplicationService.addItem(USER, milkRequest);

        UserCartItemAddRequest breadRequest = new UserCartItemAddRequest();
        breadRequest.setProductId("1002");
        breadRequest.setQuantity(1);
        userCartApplicationService.addItem(USER, breadRequest);

        List<UserCartItemResponse> cartItems = userCartApplicationService.listItems(USER);
        UserCartItemResponse selected = cartItems.stream()
                .filter(item -> "1001".equals(item.getProductId()))
                .findFirst()
                .orElseThrow();
        UserOrderSubmitRequest submitRequest = new UserOrderSubmitRequest();
        submitRequest.setCartItemIds(List.of(selected.getId()));

        UserOrderSubmitResponse response = userOrderApplicationService.submit(USER, submitRequest);

        assertEquals(1, response.getItemCount());
        assertEquals(new BigDecimal("8.75"), response.getTotalAmount());
        List<UserCartItemResponse> remainingItems = userCartApplicationService.listItems(USER);
        assertEquals(1, remainingItems.size());
        assertEquals("1002", remainingItems.get(0).getProductId());
    }

    @Test
    void submitShouldRejectWhenCartEmpty() {
        UserOrderSubmitRequest request = new UserOrderSubmitRequest();

        assertThrows(ApplicationException.class, () -> userOrderApplicationService.submit(USER, request));
    }

    @Test
    void submitShouldRejectWhenPickupPhoneMissing() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        request.setQuantity(1);
        userCartApplicationService.addItem(EMAIL_ONLY_USER, request);

        assertThrows(ApplicationException.class, () -> userOrderApplicationService.submit(EMAIL_ONLY_USER, new UserOrderSubmitRequest()));
    }

    @Test
    void submitShouldRejectWhenStockInsufficient() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        request.setQuantity(1);
        userCartApplicationService.addItem(USER, request);

        userOrderPersistenceMapper.incrementBatchLockedQuantity(2001L, 15, LocalDateTime.now());

        UserOrderSubmitRequest submitRequest = new UserOrderSubmitRequest();
        assertThrows(ApplicationException.class, () -> userOrderApplicationService.submit(USER, submitRequest));
    }

    @Test
    void pageOrdersShouldSupportStatusFilter() {
        UserOrderQuery query = new UserOrderQuery();
        query.setPage(1);
        query.setPageSize(10);
        query.setStatus("pending_payment");

        PageResponse<UserOrderSummaryResponse> page = userOrderApplicationService.pageOrders(USER, query);

        assertEquals(1, page.getTotal());
        assertEquals("5001", page.getItems().get(0).getId());
    }

    @Test
    void getOrderDetailShouldReturnOwnedOrder() {
        UserOrderDetailResponse response = userOrderApplicationService.getOrderDetail(USER, "5001");

        assertEquals("5001", response.getId());
        assertEquals("SFU202605130001", response.getOrderNumber());
        assertEquals(2, response.getItemCount());
        assertFalse(response.getItems().isEmpty());
        assertFalse(response.getEvents().isEmpty());
        assertEquals("submitted", response.getEvents().get(0).getEventType().value());
    }

    @Test
    void cancelOrderShouldReleaseLockedStockAndMarkCancelled() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        request.setQuantity(2);
        userCartApplicationService.addItem(USER, request);

        UserOrderSubmitResponse submitResponse = userOrderApplicationService.submit(USER, new UserOrderSubmitRequest());
        UserOrderCancelRequest cancelRequest = new UserOrderCancelRequest();
        cancelRequest.setCancelReason("临时有事，稍后再买");
        userOrderApplicationService.cancelOrder(USER, submitResponse.getId(), cancelRequest);

        UserOrderDetailResponse detailResponse = userOrderApplicationService.getOrderDetail(USER, submitResponse.getId());
        assertEquals("cancelled", detailResponse.getStatus().value());
        assertEquals("临时有事，稍后再买", detailResponse.getCancelReason());
        assertEquals("cancelled", detailResponse.getEvents().get(detailResponse.getEvents().size() - 1).getEventType().value());
        assertEquals("临时有事，稍后再买", detailResponse.getEvents().get(detailResponse.getEvents().size() - 1).getNote());
    }

    @Test
    void payOrderShouldAdvanceStatusAndPayState() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        request.setQuantity(1);
        userCartApplicationService.addItem(USER, request);

        UserOrderSubmitResponse submitResponse = userOrderApplicationService.submit(USER, new UserOrderSubmitRequest());
        UserOrderDetailResponse detailResponse = userOrderApplicationService.payOrder(USER, submitResponse.getId());

        assertEquals("to_prepare", detailResponse.getStatus().value());
        assertEquals("paid", detailResponse.getPayStatus().value());
        assertNotNull(detailResponse.getCheckoutTime());
        assertEquals("paid", detailResponse.getEvents().get(detailResponse.getEvents().size() - 1).getEventType().value());
    }

    @Test
    void payOrderShouldBeIdempotentWhenAlreadyPaid() {
        UserOrderDetailResponse detailResponse = userOrderApplicationService.payOrder(USER, "5001");
        assertEquals("to_prepare", detailResponse.getStatus().value());

        UserOrderDetailResponse repeatedResponse = userOrderApplicationService.payOrder(USER, "5001");
        assertEquals("to_prepare", repeatedResponse.getStatus().value());
        assertEquals("paid", repeatedResponse.getPayStatus().value());
        UserOrderPaymentDataObject payment = userOrderPersistenceMapper.findOrderPaymentByOrderId(5001L);
        assertNotNull(payment);
        assertEquals(1, payment.getStatus());
        assertEquals("PAYSFU202605130001", payment.getPaymentNo());
        assertEquals("user-order-pay:4001:5001", payment.getIdempotencyKey());
    }

    @Test
    void paymentCallbackShouldConfirmPendingOrderAndBeIdempotent() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        request.setQuantity(1);
        userCartApplicationService.addItem(USER, request);

        UserOrderSubmitResponse submitResponse = userOrderApplicationService.submit(USER, new UserOrderSubmitRequest());
        UserPaymentCallbackRequest callbackRequest = buildPaymentCallbackRequest(submitResponse.getOrderNumber(), submitResponse.getTotalAmount());

        UserPaymentCallbackResponse callbackResponse = userOrderApplicationService.confirmPaymentCallback(
                callbackRequest,
                sign(callbackRequest)
        );
        UserPaymentCallbackResponse repeatedResponse = userOrderApplicationService.confirmPaymentCallback(
                callbackRequest,
                sign(callbackRequest)
        );

        assertEquals("paid", callbackResponse.getPayStatus());
        assertEquals("to_prepare", callbackResponse.getOrderStatus());
        assertFalse(callbackResponse.isDuplicate());
        assertEquals("paid", repeatedResponse.getPayStatus());
        assertEquals(true, repeatedResponse.isDuplicate());
        UserOrderPaymentDataObject payment = userOrderPersistenceMapper.findOrderPaymentByOrderId(Long.valueOf(submitResponse.getId()));
        assertNotNull(payment);
        assertEquals("mock-trade-" + submitResponse.getOrderNumber(), payment.getExternalTradeNo());
        assertEquals("mock-callback-" + submitResponse.getOrderNumber(), payment.getCallbackEventId());
    }

    @Test
    void paymentCallbackShouldRejectInvalidSignature() {
        UserPaymentCallbackRequest callbackRequest = buildPaymentCallbackRequest("SFU202605130001", new BigDecimal("26.25"));

        assertThrows(ApplicationException.class, () -> userOrderApplicationService.confirmPaymentCallback(callbackRequest, "bad-signature"));
    }

    @Test
    void timeoutCloseShouldCancelExpiredPendingPaymentOrdersAndReleaseStock() {
        int closedCount = userOrderTimeoutCloseService.closeExpiredPendingPaymentOrders(LocalDateTime.now().plusHours(1));

        assertEquals(1, closedCount);
        UserOrderDetailResponse detailResponse = userOrderApplicationService.getOrderDetail(USER, "5001");
        assertEquals("cancelled", detailResponse.getStatus().value());
        assertEquals("订单超时未支付，系统自动取消", detailResponse.getCancelReason());
        assertEquals("system", detailResponse.getEvents().get(detailResponse.getEvents().size() - 1).getActorType().value());
        assertEquals("cancelled", detailResponse.getEvents().get(detailResponse.getEvents().size() - 1).getEventType().value());
    }

    private UserPaymentCallbackRequest buildPaymentCallbackRequest(String orderNumber, BigDecimal amount) {
        UserPaymentCallbackRequest request = new UserPaymentCallbackRequest();
        request.setPaymentNo("PAY" + orderNumber);
        request.setExternalTradeNo("mock-trade-" + orderNumber);
        request.setCallbackEventId("mock-callback-" + orderNumber);
        request.setProvider("mock");
        request.setStatus("succeeded");
        request.setAmount(amount);
        request.setPaidTime(LocalDateTime.now());
        return request;
    }

    private String sign(UserPaymentCallbackRequest request) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(PAYMENT_CALLBACK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String payload = request.getCallbackEventId() + "\n"
                    + request.getPaymentNo() + "\n"
                    + request.getExternalTradeNo() + "\n"
                    + request.getStatus() + "\n"
                    + request.getAmount().stripTrailingZeros().toPlainString();
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
