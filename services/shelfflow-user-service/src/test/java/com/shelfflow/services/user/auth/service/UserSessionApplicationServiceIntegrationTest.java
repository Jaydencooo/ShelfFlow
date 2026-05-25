package com.shelfflow.services.user.auth.service;

import com.shelfflow.services.common.dto.UserLoginRequest;
import com.shelfflow.services.common.dto.UserPasswordResetRequest;
import com.shelfflow.services.common.dto.UserProfileUpdateRequest;
import com.shelfflow.services.common.dto.UserRegisterRequest;
import com.shelfflow.services.common.dto.UserSessionResponse;
import com.shelfflow.services.common.security.UserAuthenticatedUser;
import com.shelfflow.services.user.ShelfFlowUserServiceApplication;
import com.shelfflow.services.user.auth.persistence.UserAccountPersistenceMapper;
import com.shelfflow.services.user.auth.persistence.dataobject.UserAccountDataObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = ShelfFlowUserServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class UserSessionApplicationServiceIntegrationTest {

    @Autowired
    private UserSessionApplicationService userSessionApplicationService;

    @Autowired
    private UserAccountPersistenceMapper userAccountPersistenceMapper;

    @Test
    void registerShouldCreateUserAndIssueToken() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setOpenId("openid-new");
        request.setName("New User");
        request.setPhone("13900000000");
        request.setPassword("Passw0rd!");

        UserSessionResponse response = userSessionApplicationService.register(request);

        assertNotNull(response.getToken());
        UserAccountDataObject saved = userAccountPersistenceMapper.findByOpenId("openid-new");
        assertEquals("New User", saved.getName());
        assertEquals("13900000000", saved.getPhone());
        assertNotNull(saved.getPasswordHash());
    }

    @Test
    void loginShouldIssueTokenForExistingUser() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setOpenId("openid-login");
        registerRequest.setName("Login User");
        registerRequest.setPhone("13900000002");
        registerRequest.setPassword("Passw0rd!");
        userSessionApplicationService.register(registerRequest);

        UserLoginRequest request = new UserLoginRequest();
        request.setOpenId("openid-login");
        request.setPassword("Passw0rd!");

        UserSessionResponse response = userSessionApplicationService.login(request);

        assertNotNull(response.getToken());
        assertEquals("openid-login", response.getOpenId());
    }

    @Test
    void resetPasswordShouldUpdatePasswordHash() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setOpenId("openid-reset-flow");
        registerRequest.setName("Recover User");
        registerRequest.setPhone("13800000001");
        registerRequest.setPassword("Passw0rd!");
        userSessionApplicationService.register(registerRequest);

        UserPasswordResetRequest request = new UserPasswordResetRequest();
        request.setOpenId("openid-reset-flow");
        request.setPhone("13800000001");
        request.setNewPassword("Reset2026!");

        userSessionApplicationService.resetPassword(request);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setOpenId("openid-reset-flow");
        loginRequest.setPassword("Reset2026!");
        UserSessionResponse response = userSessionApplicationService.login(loginRequest);

        assertEquals("openid-reset-flow", response.getOpenId());
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setOpenId("openid-invalid");
        registerRequest.setName("Invalid User");
        registerRequest.setPhone("13900000003");
        registerRequest.setPassword("Passw0rd!");
        userSessionApplicationService.register(registerRequest);

        UserLoginRequest request = new UserLoginRequest();
        request.setOpenId("openid-invalid");
        request.setPassword("Wrong2026!");

        assertThrows(RuntimeException.class, () -> userSessionApplicationService.login(request));
    }

    @Test
    void currentSessionShouldReturnPersistedUserProfile() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setOpenId("openid-session");
        request.setName("Seed User");
        request.setPhone("13800000000");
        request.setPassword("Passw0rd!");
        UserSessionResponse registered = userSessionApplicationService.register(request);

        UserSessionResponse response = userSessionApplicationService.currentSession(
                new UserAuthenticatedUser(Long.parseLong(registered.getUserId()), "openid-session", "token")
        );

        assertEquals(registered.getUserId(), response.getUserId());
        assertEquals("openid-session", response.getOpenId());
        assertEquals("Seed User", response.getName());
    }

    @Test
    void updateProfileShouldPersistNameAndPhone() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setOpenId("openid-profile");
        request.setName("Old User");
        request.setPhone("13800000004");
        request.setPassword("Passw0rd!");
        UserSessionResponse registered = userSessionApplicationService.register(request);

        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
        updateRequest.setName("Updated User");
        updateRequest.setPhone("13800000005");
        UserSessionResponse response = userSessionApplicationService.updateProfile(
                new UserAuthenticatedUser(Long.parseLong(registered.getUserId()), "openid-profile", "token"),
                updateRequest
        );

        assertEquals("Updated User", response.getName());
        assertEquals("13800000005", response.getPhone());

        UserAccountDataObject saved = userAccountPersistenceMapper.findByOpenId("openid-profile");
        assertEquals("Updated User", saved.getName());
        assertEquals("13800000005", saved.getPhone());
    }
}
