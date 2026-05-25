package com.shelfflow.services.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shelfflow.services.auth.client.LegacyStaffClient;
import com.shelfflow.services.common.dto.AdminLoginRequest;
import com.shelfflow.services.common.legacy.LegacyEnvelope;
import com.shelfflow.services.common.security.AdminAccessTokenParser;
import com.shelfflow.services.common.security.AdminAuthorizationProfile;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminAuthControllerTest {

    private static final String REQUEST_ID = "req-auth-001";
    private static final String TOKEN = "header.payload.signature";

    @Mock
    private LegacyStaffClient legacyStaffClient;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminAuthController controller = new AdminAuthController(
                legacyStaffClient,
                new AdminAccessTokenParser(objectMapper),
                new AdminAuthorizationProfile()
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void loginShouldReturnAdminSession() throws Exception {
        LegacyStaffClient.LegacyStaffLoginResponse session = new LegacyStaffClient.LegacyStaffLoginResponse();
        session.setId(7L);
        session.setUserName("admin");
        session.setName("管理员");

        Map<String, Object> payload = Map.of("empId", 7);
        session.setToken(buildToken(payload));

        LegacyEnvelope<LegacyStaffClient.LegacyStaffLoginResponse> envelope = new LegacyEnvelope<>();
        envelope.setCode(1);
        envelope.setData(session);
        envelope.setMsg("ok");

        when(legacyStaffClient.login(any(AdminLoginRequest.class))).thenReturn(envelope);

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", REQUEST_ID)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
                .andExpect(jsonPath("$.data.userId").value("7"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.roles[0]").value("admin"))
                .andExpect(jsonPath("$.data.permissions[0]").value("product:read"));
    }

    @Test
    void loginShouldMapLegacyFailureToDependencyError() throws Exception {
        LegacyEnvelope<LegacyStaffClient.LegacyStaffLoginResponse> envelope = new LegacyEnvelope<>();
        envelope.setCode(0);
        envelope.setMsg("账号或密码错误");

        when(legacyStaffClient.login(any(AdminLoginRequest.class))).thenReturn(envelope);

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", REQUEST_ID)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "wrong123"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("dependency_error"))
                .andExpect(jsonPath("$.message").value("账号或密码错误"));
    }

    @Test
    void loginShouldRejectLegacySessionWithoutToken() throws Exception {
        LegacyStaffClient.LegacyStaffLoginResponse session = new LegacyStaffClient.LegacyStaffLoginResponse();
        session.setId(7L);
        session.setUserName("admin");
        session.setName("管理员");
        session.setToken("  ");

        LegacyEnvelope<LegacyStaffClient.LegacyStaffLoginResponse> envelope = new LegacyEnvelope<>();
        envelope.setCode(1);
        envelope.setData(session);
        envelope.setMsg("ok");

        when(legacyStaffClient.login(any(AdminLoginRequest.class))).thenReturn(envelope);

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", REQUEST_ID)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("dependency_error"))
                .andExpect(jsonPath("$.message").value("legacy login returned invalid session"));
    }

    @Test
    void meShouldReturnAdminSession() throws Exception {
        LegacyStaffClient.LegacyStaffResponse staff = new LegacyStaffClient.LegacyStaffResponse();
        staff.setId(9L);
        staff.setUsername("shelfflow-admin");
        staff.setName("运营管理员");

        LegacyEnvelope<LegacyStaffClient.LegacyStaffResponse> envelope = new LegacyEnvelope<>();
        envelope.setCode(1);
        envelope.setData(staff);
        envelope.setMsg("ok");

        String token = buildToken(Map.of("empId", 9));
        when(legacyStaffClient.getById(9L, token)).thenReturn(envelope);

        mockMvc.perform(get("/api/admin/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ok"))
                .andExpect(jsonPath("$.data.userId").value("9"))
                .andExpect(jsonPath("$.data.permissions[3]").value("inventory:write"));

        verify(legacyStaffClient).getById(9L, token);
    }

    @Test
    void meShouldMapLegacyFailureToDependencyError() throws Exception {
        LegacyEnvelope<LegacyStaffClient.LegacyStaffResponse> envelope = new LegacyEnvelope<>();
        envelope.setCode(0);
        envelope.setMsg("legacy profile unavailable");

        String token = buildToken(Map.of("empId", 9));
        when(legacyStaffClient.getById(9L, token)).thenReturn(envelope);

        mockMvc.perform(get("/api/admin/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("dependency_error"))
                .andExpect(jsonPath("$.message").value("legacy profile unavailable"));
    }

    @Test
    void meShouldRejectTokenWithoutEmpId() throws Exception {
        String token = buildToken(Map.of("sub", "9"));

        mockMvc.perform(get("/api/admin/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"));
    }

    @Test
    void meShouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/admin/auth/me")
                        .header("Authorization", "Bearer broken-token")
                        .header("X-Request-Id", REQUEST_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"));
    }

    private String buildToken(Map<String, Object> payload) throws Exception {
        String header = base64Url(objectMapper.writeValueAsBytes(Map.of("alg", "none", "typ", "JWT")));
        String body = base64Url(objectMapper.writeValueAsBytes(payload));
        return header + "." + body + ".signature";
    }

    private String base64Url(byte[] bytes) {
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
