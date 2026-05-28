package com.shelfflow.services.admin.controller;

import com.shelfflow.services.admin.aiops.service.AdminAiOpsApplicationService;
import com.shelfflow.services.common.api.ApiResponse;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.dto.AdminAiKnowledgeQuery;
import com.shelfflow.services.common.dto.AdminAiKnowledgeResponse;
import com.shelfflow.services.common.dto.AdminAiKnowledgeUpsertRequest;
import com.shelfflow.services.common.dto.AdminAiOpsChatMessageResponse;
import com.shelfflow.services.common.dto.AdminAiOpsChatRequest;
import com.shelfflow.services.common.dto.AdminAiOpsChatResponse;
import com.shelfflow.services.common.dto.AdminAiOpsSuggestionActionRequest;
import com.shelfflow.services.common.dto.AdminAiOpsSuggestionActionResponse;
import com.shelfflow.services.common.dto.AdminAiOpsSuggestionResponse;
import com.shelfflow.services.common.security.AdminAuthenticatedUser;
import com.shelfflow.services.common.security.AdminPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/admin/ai-ops")
public class AdminAiOpsController {

    private final AdminAiOpsApplicationService aiOpsApplicationService;

    public AdminAiOpsController(AdminAiOpsApplicationService aiOpsApplicationService) {
        this.aiOpsApplicationService = aiOpsApplicationService;
    }

    @GetMapping("/suggestions")
    public ApiResponse<List<AdminAiOpsSuggestionResponse>> suggestions(AdminAuthenticatedUser authenticatedUser,
                                                                       HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.AI_OPS_READ);
        return ApiResponse.success(aiOpsApplicationService.suggestions(), requestId(request), "查询成功");
    }

    @PostMapping("/suggestions/{id}/action")
    public ApiResponse<Void> updateSuggestionAction(AdminAuthenticatedUser authenticatedUser,
                                                    @PathVariable String id,
                                                    @Valid @RequestBody AdminAiOpsSuggestionActionRequest requestBody,
                                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.AI_OPS_WRITE);
        aiOpsApplicationService.updateSuggestionAction(
                authenticatedUser.getUserId(),
                id,
                requestBody.getAction(),
                requestBody.getBatchStatus(),
                requestBody.getOperationNote()
        );
        return ApiResponse.success(null, requestId(request), "建议处理成功");
    }

    @GetMapping("/suggestions/actions")
    public ApiResponse<List<AdminAiOpsSuggestionActionResponse>> suggestionActions(AdminAuthenticatedUser authenticatedUser,
                                                                                   HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.AI_OPS_READ);
        return ApiResponse.success(aiOpsApplicationService.suggestionActions(), requestId(request), "查询成功");
    }

    @GetMapping("/knowledge")
    public ApiResponse<PageResponse<AdminAiKnowledgeResponse>> pageKnowledge(AdminAuthenticatedUser authenticatedUser,
                                                                             @Valid @ModelAttribute AdminAiKnowledgeQuery query,
                                                                             HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.AI_OPS_READ);
        return ApiResponse.success(aiOpsApplicationService.pageKnowledge(query), requestId(request), "查询成功");
    }

    @PostMapping("/knowledge")
    public ApiResponse<AdminAiKnowledgeResponse> createKnowledge(AdminAuthenticatedUser authenticatedUser,
                                                                 @Valid @RequestBody AdminAiKnowledgeUpsertRequest requestBody,
                                                                 HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.AI_OPS_WRITE);
        return ApiResponse.success(
                aiOpsApplicationService.createKnowledge(authenticatedUser.getUserId(), requestBody),
                requestId(request),
                "知识条目创建成功"
        );
    }

    @PutMapping("/knowledge/{id}")
    public ApiResponse<AdminAiKnowledgeResponse> updateKnowledge(AdminAuthenticatedUser authenticatedUser,
                                                                 @PathVariable String id,
                                                                 @Valid @RequestBody AdminAiKnowledgeUpsertRequest requestBody,
                                                                 HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.AI_OPS_WRITE);
        return ApiResponse.success(
                aiOpsApplicationService.updateKnowledge(authenticatedUser.getUserId(), id, requestBody),
                requestId(request),
                "知识条目更新成功"
        );
    }

    @DeleteMapping("/knowledge/{id}")
    public ApiResponse<Void> deleteKnowledge(AdminAuthenticatedUser authenticatedUser,
                                             @PathVariable String id,
                                             HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.AI_OPS_WRITE);
        aiOpsApplicationService.deleteKnowledge(id);
        return ApiResponse.success(null, requestId(request), "知识条目删除成功");
    }

    @PostMapping("/chat")
    public ApiResponse<AdminAiOpsChatResponse> chat(AdminAuthenticatedUser authenticatedUser,
                                                    @Valid @RequestBody AdminAiOpsChatRequest requestBody,
                                                    HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.AI_OPS_READ);
        return ApiResponse.success(aiOpsApplicationService.chat(authenticatedUser.getUserId(), requestBody.getSessionId(), requestBody.getMessage()), requestId(request), "问答完成");
    }

    @GetMapping("/chat/history")
    public ApiResponse<List<AdminAiOpsChatMessageResponse>> chatHistory(AdminAuthenticatedUser authenticatedUser,
                                                                        @RequestParam(required = false) String sessionId,
                                                                        HttpServletRequest request) {
        authenticatedUser.requirePermission(AdminPermission.AI_OPS_READ);
        return ApiResponse.success(aiOpsApplicationService.chatHistory(authenticatedUser.getUserId(), sessionId), requestId(request), "查询成功");
    }

    private String requestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}
