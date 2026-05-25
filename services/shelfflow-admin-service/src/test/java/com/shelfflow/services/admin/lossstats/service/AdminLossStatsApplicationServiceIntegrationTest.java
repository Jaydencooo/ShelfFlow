package com.shelfflow.services.admin.lossstats.service;

import com.shelfflow.services.common.dto.AdminLossStatsOverviewResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminLossStatsApplicationServiceIntegrationTest {

    @Autowired
    private AdminLossStatsApplicationService lossStatsApplicationService;

    @Test
    void overviewShouldAggregateLossStats() {
        AdminLossStatsOverviewResponse response = lossStatsApplicationService.overview();

        assertThat(response.getTotalBatchCount()).isGreaterThanOrEqualTo(2L);
        assertThat(response.getExpiringSoonBatchCount()).isGreaterThanOrEqualTo(1L);
        assertThat(response.getExpiringSoonStockQuantity()).isGreaterThanOrEqualTo(1L);
        assertThat(response.getCategoryStats()).isNotEmpty();
        assertThat(response.getSuggestions()).isNotEmpty();
    }
}
