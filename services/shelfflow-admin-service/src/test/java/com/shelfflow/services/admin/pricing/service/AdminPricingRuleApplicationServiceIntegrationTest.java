package com.shelfflow.services.admin.pricing.service;

import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.domain.PricingRuleStatus;
import com.shelfflow.services.common.dto.AdminPricingRuleQuery;
import com.shelfflow.services.common.dto.AdminPricingRuleResponse;
import com.shelfflow.services.common.dto.AdminPricingRuleUpsertRequest;
import com.shelfflow.services.common.dto.AdminPricingSuggestionResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class AdminPricingRuleApplicationServiceIntegrationTest {

    private static final long ACTOR_ID = 9001L;

    @Autowired
    private AdminPricingRuleApplicationService pricingRuleApplicationService;

    @Test
    void pageShouldReturnPricingRules() {
        AdminPricingRuleQuery query = new AdminPricingRuleQuery();
        query.setPage(1);
        query.setPageSize(10);

        PageResponse<AdminPricingRuleResponse> page = pricingRuleApplicationService.page(query);

        assertTrue(page.getTotal() >= 2);
        assertFalse(page.getItems().isEmpty());
    }

    @Test
    void createShouldPersistPricingRule() {
        AdminPricingRuleResponse created = pricingRuleApplicationService.create(ACTOR_ID, buildRequest("测试规则", 1, 2));

        assertNotNull(created.getId());
        assertEquals("测试规则", created.getName());
        assertEquals(PricingRuleStatus.ENABLED, created.getStatus());
    }

    @Test
    void updateStatusShouldDisableRule() {
        AdminPricingRuleResponse updated = pricingRuleApplicationService.updateStatus(ACTOR_ID, "3001", PricingRuleStatus.DISABLED);

        assertEquals(PricingRuleStatus.DISABLED, updated.getStatus());
    }

    @Test
    void createShouldRejectInvalidDayRange() {
        AdminPricingRuleUpsertRequest request = buildRequest("非法规则", 10, 1);

        assertThrows(ApplicationException.class, () -> pricingRuleApplicationService.create(ACTOR_ID, request));
    }

    @Test
    void suggestionsShouldReturnNearExpiryBatches() {
        List<AdminPricingSuggestionResponse> suggestions = pricingRuleApplicationService.suggestions();

        assertFalse(suggestions.isEmpty());
        assertNotNull(suggestions.get(0).getSuggestedPrice());
    }

    private AdminPricingRuleUpsertRequest buildRequest(String name, int minDaysToExpire, int maxDaysToExpire) {
        AdminPricingRuleUpsertRequest request = new AdminPricingRuleUpsertRequest();
        request.setName(name);
        request.setMinDaysToExpire(minDaysToExpire);
        request.setMaxDaysToExpire(maxDaysToExpire);
        request.setDiscountRate(new BigDecimal("0.80"));
        request.setPriority(50);
        request.setStatus(PricingRuleStatus.ENABLED);
        return request;
    }
}
