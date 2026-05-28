package com.shelfflow.services.user.auth.service;

import com.shelfflow.services.common.dto.UserLoginRequest;
import com.shelfflow.services.common.dto.UserPasswordChangeRequest;
import com.shelfflow.services.common.dto.UserPasswordResetRequest;
import com.shelfflow.services.common.dto.UserProfileUpdateRequest;
import com.shelfflow.services.common.dto.UserRegisterRequest;
import com.shelfflow.services.common.dto.UserSessionResponse;
import com.shelfflow.services.common.dto.UserVerificationCodeRequest;
import com.shelfflow.services.common.dto.UserVerificationCodeResponse;
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

    @Autowired
    private UserVerificationCodeApplicationService userVerificationCodeApplicationService;

    @Test
    void registerShouldCreateUserAndIssueToken() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setAccount("13900000000");
        request.setName("New User");
        request.setPassword("Passw0rd!");
        request.setConfirmPassword("Passw0rd!");
        request.setVerificationCode(sendRegisterCode("13900000000"));

        UserSessionResponse response = userSessionApplicationService.register(request);

        assertNotNull(response.getToken());
        UserAccountDataObject saved = userAccountPersistenceMapper.findByOpenId("13900000000");
        assertEquals("New User", saved.getName());
        assertEquals("13900000000", saved.getPhone());
        assertNotNull(saved.getPasswordHash());
    }

    @Test
    void loginShouldIssueTokenForExistingUser() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setAccount("login@example.com");
        registerRequest.setName("Login User");
        registerRequest.setPassword("Passw0rd!");
        registerRequest.setConfirmPassword("Passw0rd!");
        registerRequest.setVerificationCode(sendRegisterCode("login@example.com"));
        userSessionApplicationService.register(registerRequest);

        UserLoginRequest request = new UserLoginRequest();
        request.setAccount("login@example.com");
        request.setPassword("Passw0rd!");

        UserSessionResponse response = userSessionApplicationService.login(request);

        assertNotNull(response.getToken());
        assertEquals("login@example.com", response.getOpenId());
    }

    @Test
    void resetPasswordShouldUpdatePasswordHash() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setAccount("13900000001");
        registerRequest.setName("Recover User");
        registerRequest.setPassword("Passw0rd!");
        registerRequest.setConfirmPassword("Passw0rd!");
        registerRequest.setVerificationCode(sendRegisterCode("13900000001"));
        userSessionApplicationService.register(registerRequest);

        UserPasswordResetRequest request = new UserPasswordResetRequest();
        request.setAccount("13900000001");
        request.setNewPassword("Reset2026!");
        request.setConfirmPassword("Reset2026!");
        request.setVerificationCode(sendResetCode("13900000001"));

        userSessionApplicationService.resetPassword(request);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setAccount("13900000001");
        loginRequest.setPassword("Reset2026!");
        UserSessionResponse response = userSessionApplicationService.login(loginRequest);

        assertEquals("13900000001", response.getOpenId());
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setAccount("13900000003");
        registerRequest.setName("Invalid User");
        registerRequest.setPassword("Passw0rd!");
        registerRequest.setConfirmPassword("Passw0rd!");
        registerRequest.setVerificationCode(sendRegisterCode("13900000003"));
        userSessionApplicationService.register(registerRequest);

        UserLoginRequest request = new UserLoginRequest();
        request.setAccount("13900000003");
        request.setPassword("Wrong2026!");

        assertThrows(RuntimeException.class, () -> userSessionApplicationService.login(request));
    }

    @Test
    void currentSessionShouldReturnPersistedUserProfile() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setAccount("session@example.com");
        request.setName("Seed User");
        request.setPassword("Passw0rd!");
        request.setConfirmPassword("Passw0rd!");
        request.setVerificationCode(sendRegisterCode("session@example.com"));
        UserSessionResponse registered = userSessionApplicationService.register(request);

        UserSessionResponse response = userSessionApplicationService.currentSession(
                new UserAuthenticatedUser(Long.parseLong(registered.getUserId()), "session@example.com", "token")
        );

        assertEquals(registered.getUserId(), response.getUserId());
        assertEquals("session@example.com", response.getOpenId());
        assertEquals("Seed User", response.getName());
    }

    @Test
    void updateProfileShouldPersistNameAndPhone() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setAccount("profile@example.com");
        request.setName("Old User");
        request.setPassword("Passw0rd!");
        request.setConfirmPassword("Passw0rd!");
        request.setVerificationCode(sendRegisterCode("profile@example.com"));
        UserSessionResponse registered = userSessionApplicationService.register(request);

        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest();
        updateRequest.setName("Updated User");
        updateRequest.setPhone("13800000005");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPhoneVerificationCode(sendChangePhoneCode("13800000005"));
        updateRequest.setEmailVerificationCode(sendChangeEmailCode("updated@example.com"));
        UserSessionResponse response = userSessionApplicationService.updateProfile(
                new UserAuthenticatedUser(Long.parseLong(registered.getUserId()), "profile@example.com", "token"),
                updateRequest
        );

        assertEquals("Updated User", response.getName());
        assertEquals("13800000005", response.getPhone());

        UserAccountDataObject saved = userAccountPersistenceMapper.findByOpenId("profile@example.com");
        assertEquals("Updated User", saved.getName());
        assertEquals("13800000005", saved.getPhone());
    }

    @Test
    void changePasswordShouldRequireCurrentPassword() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setAccount("change-password@example.com");
        request.setName("Password User");
        request.setPassword("Passw0rd!");
        request.setConfirmPassword("Passw0rd!");
        request.setVerificationCode(sendRegisterCode("change-password@example.com"));
        UserSessionResponse registered = userSessionApplicationService.register(request);

        UserPasswordChangeRequest changeRequest = new UserPasswordChangeRequest();
        changeRequest.setCurrentPassword("Passw0rd!");
        changeRequest.setNewPassword("NewPassw0rd!");
        changeRequest.setConfirmPassword("NewPassw0rd!");
        userSessionApplicationService.changePassword(
                new UserAuthenticatedUser(Long.parseLong(registered.getUserId()), "change-password@example.com", "token"),
                changeRequest
        );

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setAccount("change-password@example.com");
        loginRequest.setPassword("NewPassw0rd!");
        UserSessionResponse response = userSessionApplicationService.login(loginRequest);
        assertEquals(registered.getUserId(), response.getUserId());
    }

    private String sendRegisterCode(String account) {
        return sendCode(account, "register");
    }

    private String sendResetCode(String account) {
        return sendCode(account, "reset_password");
    }

    private String sendChangePhoneCode(String account) {
        return sendCode(account, "change_phone");
    }

    private String sendChangeEmailCode(String account) {
        return sendCode(account, "change_email");
    }

    private String sendCode(String account, String purpose) {
        UserVerificationCodeRequest request = new UserVerificationCodeRequest();
        request.setAccount(account);
        request.setPurpose(purpose);
        UserVerificationCodeResponse response = userVerificationCodeApplicationService.send(request);
        return response.getDebugCode();
    }
}
