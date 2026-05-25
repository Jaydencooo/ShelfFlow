package com.shelfflow.services.auth.client;

import com.shelfflow.services.common.dto.AdminLoginRequest;
import com.shelfflow.services.common.legacy.LegacyEnvelope;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "legacyStaffClient", url = "${shelfflow.legacy.base-url}")
public interface LegacyStaffClient {

    @PostMapping("/admin/staff/login")
    LegacyEnvelope<LegacyStaffLoginResponse> login(@RequestBody AdminLoginRequest request);

    @GetMapping("/admin/staff/{id}")
    LegacyEnvelope<LegacyStaffResponse> getById(@PathVariable("id") Long id, @RequestHeader("token") String token);

    @Data
    class LegacyStaffLoginResponse {
        private Long id;
        private String userName;
        private String name;
        private String token;
    }

    @Data
    class LegacyStaffResponse {
        private Long id;
        private String username;
        private String name;
    }
}
