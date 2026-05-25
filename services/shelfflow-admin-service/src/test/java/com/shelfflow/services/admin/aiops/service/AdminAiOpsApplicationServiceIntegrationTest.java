package com.shelfflow.services.admin.aiops.service;

import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminAiKnowledgeQuery;
import com.shelfflow.services.common.dto.AdminAiKnowledgeResponse;
import com.shelfflow.services.common.dto.AdminAiKnowledgeUpsertRequest;
import com.shelfflow.services.common.dto.AdminAiOpsChatResponse;
import com.shelfflow.services.common.dto.AdminAiOpsSuggestionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminAiOpsApplicationServiceIntegrationTest {

    @Autowired
    private AdminAiOpsApplicationService aiOpsApplicationService;

    @Test
    void pageKnowledgeShouldReturnSeedKnowledge() {
        AdminAiKnowledgeQuery query = new AdminAiKnowledgeQuery();
        query.setPage(1);
        query.setPageSize(10);

        PageResponse<AdminAiKnowledgeResponse> response = aiOpsApplicationService.pageKnowledge(query);

        assertThat(response.getTotal()).isGreaterThanOrEqualTo(2L);
        assertThat(response.getItems()).extracting(AdminAiKnowledgeResponse::getTitle).allMatch(title -> !title.isBlank());
    }

    @Test
    void createUpdateAndDeleteKnowledgeShouldPersistChanges() {
        AdminAiKnowledgeUpsertRequest createRequest = new AdminAiKnowledgeUpsertRequest();
        createRequest.setTitle("测试知识");
        createRequest.setCategory("测试分类");
        createRequest.setContent("测试内容");

        AdminAiKnowledgeResponse created = aiOpsApplicationService.createKnowledge(1L, createRequest);
        assertThat(created.getId()).isNotBlank();

        AdminAiKnowledgeUpsertRequest updateRequest = new AdminAiKnowledgeUpsertRequest();
        updateRequest.setTitle("测试知识更新");
        updateRequest.setCategory("测试分类");
        updateRequest.setContent("更新后的测试内容");
        AdminAiKnowledgeResponse updated = aiOpsApplicationService.updateKnowledge(1L, created.getId(), updateRequest);
        assertThat(updated.getTitle()).isEqualTo("测试知识更新");

        aiOpsApplicationService.deleteKnowledge(created.getId());
        AdminAiKnowledgeQuery query = new AdminAiKnowledgeQuery();
        query.setKeyword("测试知识更新");
        assertThat(aiOpsApplicationService.pageKnowledge(query).getTotal()).isZero();
    }

    @Test
    void suggestionsShouldReturnOperationalAdvice() {
        List<AdminAiOpsSuggestionResponse> suggestions = aiOpsApplicationService.suggestions();

        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.get(0).getSuggestedAction()).isNotBlank();
    }

    @Test
    void chatShouldUseKnowledgeAndSuggestions() {
        AdminAiOpsChatResponse response = aiOpsApplicationService.chat("乳制品临期如何处理");

        assertThat(response.getProvider()).isEqualTo("local");
        assertThat(response.getAnswer()).contains("当前优先关注");
        assertThat(response.getReferences()).isNotEmpty();
    }
}
