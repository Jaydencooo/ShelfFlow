package com.shelfflow.services.user.pickupcontact.service;

import com.shelfflow.services.common.dto.UserPickupContactRequest;
import com.shelfflow.services.common.dto.UserPickupContactResponse;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.ShelfFlowUserServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ShelfFlowUserServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class UserPickupContactApplicationServiceIntegrationTest {

    private static final UserAuthenticatedUser USER = new UserAuthenticatedUser(1001L, "openid-1001", "token");

    @Autowired
    private UserPickupContactApplicationService userPickupContactApplicationService;

    @Test
    void createShouldMakeFirstContactDefault() {
        UserPickupContactResponse response = userPickupContactApplicationService.create(USER, request("张三", "13800001111", false));

        assertTrue(response.isDefaultContact());

        List<UserPickupContactResponse> contacts = userPickupContactApplicationService.list(USER);
        assertEquals(1, contacts.size());
        assertEquals("张三", contacts.get(0).getConsignee());
    }

    @Test
    void setDefaultShouldClearOtherDefaultContacts() {
        UserPickupContactResponse first = userPickupContactApplicationService.create(USER, request("张三", "13800001111", false));
        UserPickupContactResponse second = userPickupContactApplicationService.create(USER, request("李四", "13800002222", true));

        assertTrue(second.isDefaultContact());

        userPickupContactApplicationService.setDefault(USER, first.getId());
        List<UserPickupContactResponse> contacts = userPickupContactApplicationService.list(USER);

        UserPickupContactResponse updatedFirst = contacts.stream().filter(contact -> contact.getId().equals(first.getId())).findFirst().orElseThrow();
        UserPickupContactResponse updatedSecond = contacts.stream().filter(contact -> contact.getId().equals(second.getId())).findFirst().orElseThrow();
        assertTrue(updatedFirst.isDefaultContact());
        assertFalse(updatedSecond.isDefaultContact());
    }

    @Test
    void deleteDefaultShouldPromoteRemainingContact() {
        UserPickupContactResponse first = userPickupContactApplicationService.create(USER, request("张三", "13800001111", false));
        userPickupContactApplicationService.create(USER, request("李四", "13800002222", false));

        userPickupContactApplicationService.delete(USER, first.getId());

        List<UserPickupContactResponse> contacts = userPickupContactApplicationService.list(USER);
        assertEquals(1, contacts.size());
        assertTrue(contacts.get(0).isDefaultContact());
        assertEquals("李四", contacts.get(0).getConsignee());
    }

    private UserPickupContactRequest request(String consignee, String phone, boolean defaultContact) {
        UserPickupContactRequest request = new UserPickupContactRequest();
        request.setConsignee(consignee);
        request.setPhone(phone);
        request.setLabel("自提");
        request.setDetail("滨江社区前置仓 A 区");
        request.setDefaultContact(defaultContact);
        return request;
    }
}
