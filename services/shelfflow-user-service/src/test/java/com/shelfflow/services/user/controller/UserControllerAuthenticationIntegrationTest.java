package com.shelfflow.services.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shelfflow.services.common.config.JwtProperties;
import com.shelfflow.services.common.dto.UserCartItemAddRequest;
import com.shelfflow.services.common.dto.UserCartItemQuantityUpdateRequest;
import com.shelfflow.services.common.dto.UserCartItemResponse;
import com.shelfflow.services.common.dto.UserLoginRequest;
import com.shelfflow.services.common.dto.UserPasswordResetRequest;
import com.shelfflow.services.common.dto.UserOrderQuery;
import com.shelfflow.services.common.dto.UserOrderSubmitRequest;
import com.shelfflow.services.common.dto.UserOrderSubmitResponse;
import com.shelfflow.services.common.dto.UserOrderDetailResponse;
import com.shelfflow.services.common.dto.UserOrderSummaryResponse;
import com.shelfflow.services.common.dto.UserRegisterRequest;
import com.shelfflow.services.common.dto.UserSessionResponse;
import com.shelfflow.services.common.security.UserAccessTokenService;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.common.security.UserAuthenticatedUserArgumentResolver;
import com.shelfflow.services.common.security.UserAuthenticationInterceptor;
import com.shelfflow.services.common.web.GlobalExceptionHandler;
import com.shelfflow.services.user.order.service.UserOrderApplicationService;
import com.shelfflow.services.user.auth.service.UserSessionApplicationService;
import com.shelfflow.services.user.cart.service.UserCartApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerAuthenticationIntegrationTest {

    private static final long USER_ID = 4001L;
    private static final String OPEN_ID = "openid-test-user";
    private static final String REQUEST_ID = "req-user-001";

    @Mock
    private UserSessionApplicationService userSessionApplicationService;

    @Mock
    private UserCartApplicationService userCartApplicationService;

    @Mock
    private UserOrderApplicationService userOrderApplicationService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;
    private UserAccessTokenService userAccessTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-user-secret");
        jwtProperties.setExpiresIn("PT12H");
        userAccessTokenService = new UserAccessTokenService(objectMapper, jwtProperties);

        UserAuthController userAuthController = new UserAuthController(userSessionApplicationService);
        UserCartController userCartController = new UserCartController(userCartApplicationService);
        UserOrderController userOrderController = new UserOrderController(userOrderApplicationService);

        mockMvc = MockMvcBuilders.standaloneSetup(userAuthController, userCartController, userOrderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new UserAuthenticatedUserArgumentResolver())
                .addMappedInterceptors(
                        new String[]{"/api/user/cart/**", "/api/user/auth/me", "/api/user/orders/**"},
                        new UserAuthenticationInterceptor(userAccessTokenService)
                )
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void loginShouldNotRequireAuthorization() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setOpenId(OPEN_ID);
        request.setPassword("Passw0rd!");

        when(userSessionApplicationService.login(any(UserLoginRequest.class))).thenReturn(
                UserSessionResponse.builder()
                        .userId(String.valueOf(USER_ID))
                        .openId(OPEN_ID)
                        .name("Test User")
                        .phone("13900000000")
                        .token("token")
                        .build()
        );

        mockMvc.perform(post("/api/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
                .andExpect(jsonPath("$.data.openId").value(OPEN_ID));

        verify(userSessionApplicationService).login(any(UserLoginRequest.class));
    }

    @Test
    void registerShouldNotRequireAuthorization() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setOpenId("new-user-1001");
        request.setName("New User");
        request.setPhone("13900000000");
        request.setPassword("Passw0rd!");

        when(userSessionApplicationService.register(any(UserRegisterRequest.class))).thenReturn(
                UserSessionResponse.builder()
                        .userId("5001")
                        .openId("new-user-1001")
                        .name("New User")
                        .phone("13900000000")
                        .token("token")
                        .build()
        );

        mockMvc.perform(post("/api/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.data.openId").value("new-user-1001"));

        verify(userSessionApplicationService).register(any(UserRegisterRequest.class));
    }

    @Test
    void resetPasswordShouldNotRequireAuthorization() throws Exception {
        UserPasswordResetRequest request = new UserPasswordResetRequest();
        request.setOpenId("recover-user-1001");
        request.setPhone("13900000001");
        request.setNewPassword("Reset2026!");

        mockMvc.perform(post("/api/user/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"));

        verify(userSessionApplicationService).resetPassword(any(UserPasswordResetRequest.class));
    }

    @Test
    void meShouldRejectWhenAuthorizationHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/user/auth/me")
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"))
                .andExpect(jsonPath("$.requestId").value(REQUEST_ID));

        verifyNoInteractions(userSessionApplicationService);
    }

    @Test
    void meShouldInjectAuthenticatedUser() throws Exception {
        when(userSessionApplicationService.currentSession(any(UserAuthenticatedUser.class))).thenReturn(
                UserSessionResponse.builder()
                        .userId(String.valueOf(USER_ID))
                        .openId(OPEN_ID)
                        .name("Seed User")
                        .phone("13800000000")
                        .token("token")
                        .build()
        );

        mockMvc.perform(get("/api/user/auth/me")
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.data.userId").value(String.valueOf(USER_ID)));

        verify(userSessionApplicationService).currentSession(argThat(argThatAuthenticatedUser()));
    }

    @Test
    void cartListShouldRejectWhenAuthorizationHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/user/cart/items")
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"));

        verifyNoInteractions(userCartApplicationService);
    }

    @Test
    void cartListShouldInjectAuthenticatedUser() throws Exception {
        when(userCartApplicationService.listItems(any(UserAuthenticatedUser.class))).thenReturn(List.of(
                UserCartItemResponse.builder()
                        .id("8001")
                        .productId("1001")
                        .batchId("2001")
                        .name("Fresh Milk")
                        .quantity(2)
                        .unitPrice(new BigDecimal("8.75"))
                        .lineAmount(new BigDecimal("17.50"))
                        .availableQuantity(15)
                        .build()
        ));

        mockMvc.perform(get("/api/user/cart/items")
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.data[0].id").value("8001"));

        verify(userCartApplicationService).listItems(argThat(argThatAuthenticatedUser()));
    }

    @Test
    void cartAddShouldInjectAuthenticatedUser() throws Exception {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        request.setQuantity(2);

        mockMvc.perform(post("/api/user/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"));

        verify(userCartApplicationService).addItem(argThat(argThatAuthenticatedUser()), eq(request));
    }

    @Test
    void cartRemoveShouldInjectAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/api/user/cart/items/8001")
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"));

        verify(userCartApplicationService).removeItem(argThat(argThatAuthenticatedUser()), eq("8001"));
    }

    @Test
    void cartUpdateQuantityShouldInjectAuthenticatedUser() throws Exception {
        UserCartItemQuantityUpdateRequest request = new UserCartItemQuantityUpdateRequest();
        request.setQuantity(3);

        mockMvc.perform(patch("/api/user/cart/items/8001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"));

        verify(userCartApplicationService).updateItemQuantity(argThat(argThatAuthenticatedUser()), eq("8001"), eq(request));
    }

    @Test
    void cartClearShouldInjectAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/api/user/cart/items")
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"));

        verify(userCartApplicationService).clear(argThat(argThatAuthenticatedUser()));
    }

    @Test
    void orderSubmitShouldRejectWhenAuthorizationHeaderMissing() throws Exception {
        UserOrderSubmitRequest request = new UserOrderSubmitRequest();
        request.setRemark("备注");

        mockMvc.perform(post("/api/user/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"));

        verifyNoInteractions(userOrderApplicationService);
    }

    @Test
    void orderSubmitShouldInjectAuthenticatedUser() throws Exception {
        UserOrderSubmitRequest request = new UserOrderSubmitRequest();
        request.setRemark("备注");
        when(userOrderApplicationService.submit(any(UserAuthenticatedUser.class), any(UserOrderSubmitRequest.class)))
                .thenReturn(UserOrderSubmitResponse.builder()
                        .id("7001")
                        .orderNumber("SFU202605140001")
                        .itemCount(2)
                        .totalAmount(new BigDecimal("17.50"))
                        .orderTime(LocalDateTime.now())
                        .build());

        mockMvc.perform(post("/api/user/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.data.id").value("7001"));

        verify(userOrderApplicationService).submit(argThat(argThatAuthenticatedUser()), eq(request));
    }

    @Test
    void orderPageShouldInjectAuthenticatedUser() throws Exception {
        when(userOrderApplicationService.pageOrders(any(UserAuthenticatedUser.class), any(UserOrderQuery.class)))
                .thenReturn(com.shelfflow.services.common.api.PageResponse.<UserOrderSummaryResponse>builder()
                        .items(List.of(UserOrderSummaryResponse.builder()
                                .id("5001")
                                .orderNumber("SFU202605130001")
                                .itemCount(2)
                                .build()))
                        .total(1)
                        .page(1)
                        .pageSize(10)
                        .build());

        mockMvc.perform(get("/api/user/orders?page=1&pageSize=10")
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.data.items[0].id").value("5001"));

        verify(userOrderApplicationService).pageOrders(argThat(argThatAuthenticatedUser()), any(UserOrderQuery.class));
    }

    @Test
    void orderDetailShouldInjectAuthenticatedUser() throws Exception {
        when(userOrderApplicationService.getOrderDetail(any(UserAuthenticatedUser.class), eq("5001")))
                .thenReturn(UserOrderDetailResponse.builder()
                        .id("5001")
                        .orderNumber("SFU202605130001")
                        .itemCount(2)
                        .build());

        mockMvc.perform(get("/api/user/orders/5001")
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.data.id").value("5001"));

        verify(userOrderApplicationService).getOrderDetail(argThat(argThatAuthenticatedUser()), eq("5001"));
    }

    @Test
    void orderCancelShouldInjectAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/api/user/orders/5001")
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"));

        verify(userOrderApplicationService).cancelOrder(argThat(argThatAuthenticatedUser()), eq("5001"), isNull());
    }

    @Test
    void orderPayShouldInjectAuthenticatedUser() throws Exception {
        when(userOrderApplicationService.payOrder(any(UserAuthenticatedUser.class), eq("5001")))
                .thenReturn(UserOrderDetailResponse.builder()
                        .id("5001")
                        .orderNumber("SFU202605130001")
                        .build());

        mockMvc.perform(post("/api/user/orders/5001/pay")
                        .header("Authorization", bearerToken())
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.data.id").value("5001"));

        verify(userOrderApplicationService).payOrder(argThat(argThatAuthenticatedUser()), eq("5001"));
    }

    private String bearerToken() {
        return "Bearer " + userAccessTokenService.issueToken(USER_ID, OPEN_ID);
    }

    private ArgumentMatcher<UserAuthenticatedUser> argThatAuthenticatedUser() {
        return user ->
                user != null
                        && USER_ID == user.getUserId()
                        && OPEN_ID.equals(user.getOpenId())
                        && user.getAccessToken() != null
                        && !user.getAccessToken().isBlank();
    }
}
