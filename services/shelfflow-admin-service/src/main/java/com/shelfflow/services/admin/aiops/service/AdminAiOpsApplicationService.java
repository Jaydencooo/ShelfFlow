package com.shelfflow.services.admin.aiops.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelfflow.services.admin.aiops.config.AdminAiOpsProperties;
import com.shelfflow.services.admin.aiops.domain.AdminAiOpsPolicy;
import com.shelfflow.services.admin.aiops.integration.AdminAiOpsLargeModelClient;
import com.shelfflow.services.admin.aiops.persistence.AdminAiOpsPersistenceMapper;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiKnowledgeCriteria;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiKnowledgeDataObject;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiOpsChatMessageDataObject;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiOpsChatSessionDataObject;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiOpsSuggestionActionDataObject;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiOpsSuggestionActionLogDataObject;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiOpsSuggestionRow;
import com.shelfflow.services.admin.inventorybatch.persistence.InventoryBatchPersistenceMapper;
import com.shelfflow.services.common.api.ErrorCode;
import com.shelfflow.services.common.api.PageResponse;
import com.shelfflow.services.common.domain.BatchStatus;
import com.shelfflow.services.common.dto.AdminAiKnowledgeQuery;
import com.shelfflow.services.common.dto.AdminAiKnowledgeResponse;
import com.shelfflow.services.common.dto.AdminAiKnowledgeUpsertRequest;
import com.shelfflow.services.common.dto.AdminAiOpsChatMessageResponse;
import com.shelfflow.services.common.dto.AdminAiOpsChatResponse;
import com.shelfflow.services.common.dto.AdminAiOpsSuggestionActionResponse;
import com.shelfflow.services.common.dto.AdminAiOpsSuggestionExecutionPlanResponse;
import com.shelfflow.services.common.dto.AdminAiOpsSuggestionResponse;
import com.shelfflow.services.common.exception.ApplicationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AdminAiOpsApplicationService {

    private static final String SORT_BY_UPDATED_AT = "updatedAt";
    private static final String SORT_BY_CREATED_AT = "createdAt";
    private static final String SORT_BY_TITLE = "title";
    private static final int CHAT_HISTORY_LIMIT = 80;
    private static final int SESSION_TITLE_MAX_LENGTH = 32;
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String SUGGESTION_STATUS_PENDING = "pending";
    private static final String SUGGESTION_STATUS_EXECUTED = "executed";
    private static final String SUGGESTION_STATUS_IGNORED = "ignored";
    private static final String SUGGESTION_ACTION_EXECUTE = "execute";
    private static final String SUGGESTION_ACTION_IGNORE = "ignore";
    private static final String SUGGESTION_TYPE_EXPIRED = "EXPIRED";
    private static final String SUGGESTION_TYPE_EXPIRING_SOON = "EXPIRING_SOON";
    private static final String SUGGESTION_TYPE_SOLD_OUT = "SOLD_OUT";
    private static final String SUGGESTION_TYPE_OVERSTOCK = "OVERSTOCK";
    private static final String TARGET_TYPE_BATCH = "inventory_batch";
    private static final int ACTION_HISTORY_LIMIT = 30;

    private final AdminAiOpsPersistenceMapper aiOpsPersistenceMapper;
    private final InventoryBatchPersistenceMapper inventoryBatchPersistenceMapper;
    private final AdminAiOpsPolicy aiOpsPolicy;
    private final AdminAiOpsLargeModelClient largeModelClient;
    private final AdminAiOpsProperties properties;
    private final ObjectMapper objectMapper;

    public AdminAiOpsApplicationService(AdminAiOpsPersistenceMapper aiOpsPersistenceMapper,
                                        InventoryBatchPersistenceMapper inventoryBatchPersistenceMapper,
                                        AdminAiOpsPolicy aiOpsPolicy,
                                        AdminAiOpsLargeModelClient largeModelClient,
                                        AdminAiOpsProperties properties,
                                        ObjectMapper objectMapper) {
        this.aiOpsPersistenceMapper = aiOpsPersistenceMapper;
        this.inventoryBatchPersistenceMapper = inventoryBatchPersistenceMapper;
        this.aiOpsPolicy = aiOpsPolicy;
        this.largeModelClient = largeModelClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminAiKnowledgeResponse> pageKnowledge(AdminAiKnowledgeQuery query) {
        AdminAiKnowledgeCriteria criteria = AdminAiKnowledgeCriteria.builder()
                .limit(query.getPageSize())
                .offset((query.getPage() - 1) * query.getPageSize())
                .keyword(blankToNull(query.getKeyword()))
                .category(blankToNull(query.getCategory()))
                .sortColumn(resolveSortColumn(query.getSortBy()))
                .sortDirection(query.getSortOrder().name())
                .build();

        List<AdminAiKnowledgeResponse> items = aiOpsPersistenceMapper.pageKnowledge(criteria).stream()
                .map(this::toKnowledgeResponse)
                .toList();

        return PageResponse.<AdminAiKnowledgeResponse>builder()
                .items(items)
                .total(aiOpsPersistenceMapper.countKnowledge(criteria))
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .build();
    }

    @Transactional
    public AdminAiKnowledgeResponse createKnowledge(Long actorId, AdminAiKnowledgeUpsertRequest request) {
        AdminAiKnowledgeDataObject knowledge = new AdminAiKnowledgeDataObject();
        LocalDateTime now = LocalDateTime.now();
        knowledge.setTitle(aiOpsPolicy.normalizeTitle(request.getTitle()));
        knowledge.setCategory(aiOpsPolicy.normalizeCategory(request.getCategory()));
        knowledge.setContent(aiOpsPolicy.normalizeContent(request.getContent()));
        knowledge.setCreateTime(now);
        knowledge.setUpdateTime(now);
        knowledge.setCreateUser(actorId);
        knowledge.setUpdateUser(actorId);
        aiOpsPersistenceMapper.insertKnowledge(knowledge);
        return toKnowledgeResponse(aiOpsPersistenceMapper.findKnowledgeById(knowledge.getId()));
    }

    @Transactional
    public AdminAiKnowledgeResponse updateKnowledge(Long actorId, String id, AdminAiKnowledgeUpsertRequest request) {
        Long knowledgeId = parseRequiredLong(id, "id");
        if (aiOpsPersistenceMapper.findKnowledgeById(knowledgeId) == null) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "知识条目不存在");
        }

        AdminAiKnowledgeDataObject knowledge = new AdminAiKnowledgeDataObject();
        knowledge.setId(knowledgeId);
        knowledge.setTitle(aiOpsPolicy.normalizeTitle(request.getTitle()));
        knowledge.setCategory(aiOpsPolicy.normalizeCategory(request.getCategory()));
        knowledge.setContent(aiOpsPolicy.normalizeContent(request.getContent()));
        knowledge.setUpdateTime(LocalDateTime.now());
        knowledge.setUpdateUser(actorId);
        aiOpsPersistenceMapper.updateKnowledge(knowledge);
        return toKnowledgeResponse(aiOpsPersistenceMapper.findKnowledgeById(knowledgeId));
    }

    @Transactional
    public void deleteKnowledge(String id) {
        Long knowledgeId = parseRequiredLong(id, "id");
        if (aiOpsPersistenceMapper.deleteKnowledge(knowledgeId) == 0) {
            throw new ApplicationException(ErrorCode.NOT_FOUND, "知识条目不存在");
        }
    }

    @Transactional(readOnly = true)
    public List<AdminAiOpsSuggestionResponse> suggestions() {
        return aiOpsPersistenceMapper.listSuggestions(properties.getSuggestionLimit()).stream()
                .map(this::toSuggestionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminAiOpsSuggestionActionResponse> suggestionActions() {
        return aiOpsPersistenceMapper.listSuggestionActionLogs(ACTION_HISTORY_LIMIT).stream()
                .map(this::toSuggestionActionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminAiOpsChatMessageResponse> chatHistory(Long actorId, String sessionId) {
        AdminAiOpsChatSessionDataObject session = resolveSession(actorId, sessionId, false);
        if (session == null) {
            return List.of();
        }
        return aiOpsPersistenceMapper.listChatMessages(session.getId(), CHAT_HISTORY_LIMIT).stream()
                .map(this::toChatMessageResponse)
                .toList();
    }

    @Transactional
    public AdminAiOpsChatResponse chat(String message) {
        return chat(1L, null, message);
    }

    @Transactional
    public AdminAiOpsChatResponse chat(Long actorId, String sessionId, String message) {
        AdminAiOpsChatSessionDataObject session = resolveSession(actorId, sessionId, true);
        saveChatMessage(session.getId(), ROLE_USER, message, null, null, List.of());

        String keyword = blankToNull(message);
        List<AdminAiKnowledgeDataObject> knowledge = aiOpsPersistenceMapper.retrieveKnowledge(keyword, properties.getRetrievalLimit());
        List<AdminAiOpsSuggestionRow> suggestions = aiOpsPersistenceMapper.listSuggestions(properties.getRetrievalLimit());
        String localAnswer = aiOpsPolicy.buildAnswer(message, knowledge, suggestions);
        String externalAnswer = largeModelClient.chat(
                aiOpsPolicy.buildSystemPrompt(),
                aiOpsPolicy.buildUserPrompt(message, knowledge, suggestions)
        ).orElse(null);

        String provider = externalAnswer == null ? "local" : properties.getProvider();
        String answer = externalAnswer == null ? localAnswer : externalAnswer;
        List<String> references = aiOpsPolicy.references(knowledge, suggestions);

        saveChatMessage(session.getId(), ROLE_ASSISTANT, answer, provider, properties.getModel(), references);
        aiOpsPersistenceMapper.touchChatSession(session.getId());

        return AdminAiOpsChatResponse.builder()
                .sessionId(String.valueOf(session.getId()))
                .provider(externalAnswer == null ? "local" : properties.getProvider())
                .model(properties.getModel())
                .answer(answer)
                .references(references)
                .build();
    }

    @Transactional
    public void updateSuggestionAction(Long actorId, String suggestionId, String action, String batchStatus, String operationNote) {
        String normalizedAction = blankToNull(action);
        if (!SUGGESTION_ACTION_EXECUTE.equals(normalizedAction) && !SUGGESTION_ACTION_IGNORE.equals(normalizedAction)) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "建议操作不支持");
        }

        AdminAiOpsSuggestionRow suggestion = findPendingSuggestion(suggestionId);
        AdminAiOpsSuggestionExecutionPlanResponse executionPlan = buildExecutionPlan(suggestion);
        String finalStatus = SUGGESTION_ACTION_EXECUTE.equals(normalizedAction) ? SUGGESTION_STATUS_EXECUTED : SUGGESTION_STATUS_IGNORED;
        String summary = SUGGESTION_ACTION_EXECUTE.equals(normalizedAction)
                ? executeSuggestion(actorId, suggestion, batchStatus, operationNote)
                : "已忽略建议：" + suggestion.getTitle();
        saveSuggestionActionLog(actorId, suggestionId, normalizedAction, finalStatus, suggestion, summary, batchStatus, operationNote, executionPlan);
        if (SUGGESTION_ACTION_EXECUTE.equals(normalizedAction)) {
            saveSuggestionAction(actorId, suggestionId, SUGGESTION_STATUS_EXECUTED);
            return;
        }
        saveSuggestionAction(actorId, suggestionId, SUGGESTION_STATUS_IGNORED);
    }

    private String executeSuggestion(Long actorId, AdminAiOpsSuggestionRow suggestion, String requestedBatchStatus, String operationNote) {
        Long batchId = suggestion.getBatchId();
        String type = suggestion.getType();
        BatchStatus targetStatus = resolveTargetBatchStatus(type, requestedBatchStatus);
        if (SUGGESTION_TYPE_EXPIRED.equals(type) || SUGGESTION_TYPE_EXPIRING_SOON.equals(type)) {
            inventoryBatchPersistenceMapper.updateStatus(batchId, targetStatus.legacyValue(), actorId, LocalDateTime.now());
            return suggestion.getBatchCode() + " 已调整为" + targetStatus.value() + buildOptionalNote(operationNote);
        }
        if (SUGGESTION_TYPE_SOLD_OUT.equals(type)) {
            inventoryBatchPersistenceMapper.updateStatus(batchId, targetStatus.legacyValue(), actorId, LocalDateTime.now());
            return suggestion.getBatchCode() + " 已调整为" + targetStatus.value() + buildOptionalNote(operationNote);
        }
        return suggestion.getBatchCode() + " 已记录运营处置：" + suggestion.getSuggestedAction() + buildOptionalNote(operationNote);
    }

    private AdminAiKnowledgeResponse toKnowledgeResponse(AdminAiKnowledgeDataObject knowledge) {
        return AdminAiKnowledgeResponse.builder()
                .id(String.valueOf(knowledge.getId()))
                .title(knowledge.getTitle())
                .category(knowledge.getCategory())
                .content(knowledge.getContent())
                .createTime(knowledge.getCreateTime())
                .updateTime(knowledge.getUpdateTime())
                .build();
    }

    private AdminAiOpsSuggestionResponse toSuggestionResponse(AdminAiOpsSuggestionRow row) {
        return AdminAiOpsSuggestionResponse.builder()
                .id(row.getType() + "-" + row.getBatchId())
                .type(row.getType())
                .priority(row.getPriority())
                .title(row.getTitle())
                .content(row.getContent())
                .productId(String.valueOf(row.getProductId()))
                .productName(row.getProductName())
                .batchId(String.valueOf(row.getBatchId()))
                .batchCode(row.getBatchCode())
                .daysToExpire(row.getDaysToExpire())
                .availableQuantity(row.getAvailableQuantity())
                .suggestedAction(row.getSuggestedAction())
                .status(SUGGESTION_STATUS_PENDING)
                .executionPlan(buildExecutionPlan(row))
                .build();
    }

    private AdminAiOpsSuggestionActionResponse toSuggestionActionResponse(AdminAiOpsSuggestionActionLogDataObject row) {
        return AdminAiOpsSuggestionActionResponse.builder()
                .id(String.valueOf(row.getId()))
                .suggestionId(row.getSuggestionId())
                .action(row.getAction())
                .status(row.getStatus())
                .targetType(row.getTargetType())
                .targetId(row.getTargetId())
                .targetName(row.getTargetName())
                .operationSummary(row.getOperationSummary())
                .operationPayload(row.getOperationPayload())
                .actorId(row.getActorId())
                .createTime(row.getCreateTime())
                .build();
    }

    private AdminAiOpsSuggestionExecutionPlanResponse buildExecutionPlan(AdminAiOpsSuggestionRow row) {
        BatchStatus defaultStatus = resolveDefaultBatchStatus(row.getType());
        String editableFields = SUGGESTION_TYPE_OVERSTOCK.equals(row.getType()) ? "operationNote" : "batchStatus,operationNote";
        return AdminAiOpsSuggestionExecutionPlanResponse.builder()
                .targetType(TARGET_TYPE_BATCH)
                .targetId(String.valueOf(row.getBatchId()))
                .targetName(row.getBatchCode())
                .operationType(SUGGESTION_TYPE_OVERSTOCK.equals(row.getType()) ? "record_operation" : "update_batch_status")
                .defaultBatchStatus(defaultStatus.value())
                .summary(buildExecutionSummary(row, defaultStatus))
                .editableFields(editableFields)
                .build();
    }

    private String buildExecutionSummary(AdminAiOpsSuggestionRow row, BatchStatus defaultStatus) {
        if (SUGGESTION_TYPE_OVERSTOCK.equals(row.getType())) {
            return "记录高库存运营处置，建议结合定价规则或组合促销处理。";
        }
        return "将批次 " + row.getBatchCode() + " 状态调整为 " + defaultStatus.value() + "。";
    }

    private BatchStatus resolveDefaultBatchStatus(String suggestionType) {
        if (SUGGESTION_TYPE_SOLD_OUT.equals(suggestionType)) {
            return BatchStatus.SOLD_OUT;
        }
        if (SUGGESTION_TYPE_EXPIRED.equals(suggestionType) || SUGGESTION_TYPE_EXPIRING_SOON.equals(suggestionType)) {
            return BatchStatus.PAUSED;
        }
        return BatchStatus.ACTIVE;
    }

    private BatchStatus resolveTargetBatchStatus(String suggestionType, String requestedBatchStatus) {
        if (SUGGESTION_TYPE_OVERSTOCK.equals(suggestionType)) {
            return BatchStatus.ACTIVE;
        }
        String normalized = blankToNull(requestedBatchStatus);
        if (normalized == null) {
            return resolveDefaultBatchStatus(suggestionType);
        }
        try {
            BatchStatus status = BatchStatus.fromValue(normalized);
            if (BatchStatus.DRAFT.equals(status)) {
                throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "执行建议不支持调整为草稿状态");
            }
            return status;
        } catch (IllegalArgumentException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "批次状态不支持");
        }
    }

    private AdminAiOpsSuggestionRow findPendingSuggestion(String suggestionId) {
        String[] parts = parseSuggestionId(suggestionId);
        Long batchId = parseRequiredLong(parts[1], "batchId");
        String type = parts[0];
        return aiOpsPersistenceMapper.listSuggestions(properties.getSuggestionLimit()).stream()
                .filter(row -> type.equals(row.getType()) && batchId.equals(row.getBatchId()))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND, "运营建议不存在或已处理"));
    }

    private AdminAiOpsChatSessionDataObject resolveSession(Long actorId, String sessionId, boolean createIfMissing) {
        Long parsedSessionId = parseOptionalLong(sessionId, "sessionId");
        AdminAiOpsChatSessionDataObject session = parsedSessionId == null
                ? aiOpsPersistenceMapper.findLatestChatSession(actorId)
                : aiOpsPersistenceMapper.findChatSessionById(parsedSessionId, actorId);
        if (session != null || !createIfMissing) {
            return session;
        }

        LocalDateTime now = LocalDateTime.now();
        AdminAiOpsChatSessionDataObject created = new AdminAiOpsChatSessionDataObject();
        created.setAdminUserId(actorId);
        created.setTitle("AI 运营问答");
        created.setCreateTime(now);
        created.setUpdateTime(now);
        aiOpsPersistenceMapper.insertChatSession(created);
        return created;
    }

    private void saveChatMessage(Long sessionId, String role, String content, String provider, String model, List<String> references) {
        AdminAiOpsChatMessageDataObject message = new AdminAiOpsChatMessageDataObject();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setProvider(provider);
        message.setModel(model);
        message.setReferencesJson(writeReferences(references));
        message.setCreateTime(LocalDateTime.now());
        aiOpsPersistenceMapper.insertChatMessage(message);
    }

    private AdminAiOpsChatMessageResponse toChatMessageResponse(AdminAiOpsChatMessageDataObject row) {
        return AdminAiOpsChatMessageResponse.builder()
                .id(String.valueOf(row.getId()))
                .sessionId(String.valueOf(row.getSessionId()))
                .role(row.getRole())
                .content(row.getContent())
                .provider(row.getProvider())
                .model(row.getModel())
                .references(readReferences(row.getReferencesJson()))
                .createTime(row.getCreateTime())
                .build();
    }

    private void saveSuggestionAction(Long actorId, String suggestionId, String status) {
        AdminAiOpsSuggestionActionDataObject action = new AdminAiOpsSuggestionActionDataObject();
        action.setSuggestionId(suggestionId);
        action.setStatus(status);
        action.setActorId(actorId);
        LocalDateTime now = LocalDateTime.now();
        action.setCreateTime(now);
        action.setUpdateTime(now);
        aiOpsPersistenceMapper.upsertSuggestionAction(action);
    }

    private void saveSuggestionActionLog(Long actorId,
                                         String suggestionId,
                                         String action,
                                         String status,
                                         AdminAiOpsSuggestionRow suggestion,
                                         String operationSummary,
                                         String batchStatus,
                                         String operationNote,
                                         AdminAiOpsSuggestionExecutionPlanResponse executionPlan) {
        AdminAiOpsSuggestionActionLogDataObject actionLog = new AdminAiOpsSuggestionActionLogDataObject();
        actionLog.setSuggestionId(suggestionId);
        actionLog.setAction(action);
        actionLog.setStatus(status);
        actionLog.setTargetType(TARGET_TYPE_BATCH);
        actionLog.setTargetId(String.valueOf(suggestion.getBatchId()));
        actionLog.setTargetName(suggestion.getBatchCode());
        actionLog.setOperationSummary(operationSummary);
        actionLog.setOperationPayload(writePayload(Map.of(
                "batchStatus", blankToNull(batchStatus) == null ? executionPlan.getDefaultBatchStatus() : batchStatus,
                "operationNote", blankToNull(operationNote) == null ? "" : operationNote,
                "executionPlan", executionPlan.getSummary()
        )));
        actionLog.setActorId(actorId);
        actionLog.setCreateTime(LocalDateTime.now());
        aiOpsPersistenceMapper.insertSuggestionActionLog(actionLog);
    }

    private String writePayload(Map<String, String> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private String buildOptionalNote(String operationNote) {
        String normalized = blankToNull(operationNote);
        return normalized == null ? "" : "，备注：" + normalized;
    }

    private String[] parseSuggestionId(String suggestionId) {
        String normalized = blankToNull(suggestionId);
        if (normalized == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "suggestionId 不能为空");
        }
        int separatorIndex = normalized.lastIndexOf('-');
        if (separatorIndex <= 0 || separatorIndex >= normalized.length() - 1) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "suggestionId 不合法");
        }
        return new String[]{normalized.substring(0, separatorIndex), normalized.substring(separatorIndex + 1)};
    }

    private String writeReferences(List<String> references) {
        try {
            return objectMapper.writeValueAsString(references == null ? List.of() : references);
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }

    private List<String> readReferences(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private String resolveSortColumn(String sortBy) {
        String normalized = blankToNull(sortBy);
        if (normalized == null || SORT_BY_UPDATED_AT.equals(normalized)) {
            return "update_time";
        }
        if (SORT_BY_CREATED_AT.equals(normalized)) {
            return "create_time";
        }
        if (SORT_BY_TITLE.equals(normalized)) {
            return "title";
        }
        throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "sortBy 不支持");
    }

    private Long parseRequiredLong(String value, String fieldName) {
        Long parsed = parseOptionalLong(value, fieldName);
        if (parsed == null) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 不能为空");
        }
        return parsed;
    }

    private Long parseOptionalLong(String value, String fieldName) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, fieldName + " 不是有效数字");
        }
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
