package com.shelfflow.services.user.cart.service;

import com.shelfflow.services.common.dto.UserCartItemAddRequest;
import com.shelfflow.services.common.dto.UserCartItemQuantityUpdateRequest;
import com.shelfflow.services.common.dto.UserCartItemResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.ShelfFlowUserServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = ShelfFlowUserServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class UserCartApplicationServiceIntegrationTest {

    private static final UserAuthenticatedUser USER = new UserAuthenticatedUser(4001L, "openid-seeded", "token");

    @Autowired
    private UserCartApplicationService userCartApplicationService;

    @Test
    void addItemShouldCreateCartEntryUsingRecommendedBatch() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        request.setQuantity(2);

        userCartApplicationService.addItem(USER, request);

        List<UserCartItemResponse> items = userCartApplicationService.listItems(USER);
        assertEquals(1, items.size());
        assertEquals("Fresh Milk", items.get(0).getName());
        assertEquals("2001", items.get(0).getBatchId());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals(new BigDecimal("17.50"), items.get(0).getLineAmount());
    }

    @Test
    void addItemShouldMergeSameSelection() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        request.setQuantity(1);

        userCartApplicationService.addItem(USER, request);
        userCartApplicationService.addItem(USER, request);

        List<UserCartItemResponse> items = userCartApplicationService.listItems(USER);
        assertEquals(1, items.size());
        assertEquals(2, items.get(0).getQuantity());
    }

    @Test
    void addItemShouldRejectWhenQuantityExceedsAvailableStock() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1002");
        request.setQuantity(10);

        assertThrows(ApplicationException.class, () -> userCartApplicationService.addItem(USER, request));
    }

    @Test
    void removeAndClearShouldOnlyAffectCurrentUserCart() {
        UserCartItemAddRequest request = new UserCartItemAddRequest();
        request.setProductId("1001");
        userCartApplicationService.addItem(USER, request);

        List<UserCartItemResponse> items = userCartApplicationService.listItems(USER);
        userCartApplicationService.removeItem(USER, items.get(0).getId());
        assertEquals(0, userCartApplicationService.listItems(USER).size());

        userCartApplicationService.addItem(USER, request);
        userCartApplicationService.clear(USER);
        assertEquals(0, userCartApplicationService.listItems(USER).size());
    }

    @Test
    void updateQuantityShouldPersistRequestedQuantity() {
        UserCartItemAddRequest addRequest = new UserCartItemAddRequest();
        addRequest.setProductId("1001");
        addRequest.setQuantity(1);
        userCartApplicationService.addItem(USER, addRequest);

        UserCartItemQuantityUpdateRequest updateRequest = new UserCartItemQuantityUpdateRequest();
        updateRequest.setQuantity(3);

        String cartItemId = userCartApplicationService.listItems(USER).get(0).getId();
        userCartApplicationService.updateItemQuantity(USER, cartItemId, updateRequest);

        List<UserCartItemResponse> items = userCartApplicationService.listItems(USER);
        assertEquals(1, items.size());
        assertEquals(3, items.get(0).getQuantity());
    }

    @Test
    void updateQuantityShouldRejectWhenQuantityExceedsAvailableStock() {
        UserCartItemAddRequest addRequest = new UserCartItemAddRequest();
        addRequest.setProductId("1001");
        userCartApplicationService.addItem(USER, addRequest);

        UserCartItemQuantityUpdateRequest updateRequest = new UserCartItemQuantityUpdateRequest();
        updateRequest.setQuantity(99);

        String cartItemId = userCartApplicationService.listItems(USER).get(0).getId();
        assertThrows(ApplicationException.class,
                () -> userCartApplicationService.updateItemQuantity(USER, cartItemId, updateRequest));
    }
}
