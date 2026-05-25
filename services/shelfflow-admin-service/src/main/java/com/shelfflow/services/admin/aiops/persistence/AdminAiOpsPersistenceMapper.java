package com.shelfflow.services.admin.aiops.persistence;

import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiKnowledgeCriteria;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiKnowledgeDataObject;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiOpsChatMessageDataObject;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiOpsChatSessionDataObject;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiOpsSuggestionActionDataObject;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiOpsSuggestionRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminAiOpsPersistenceMapper {

    List<AdminAiKnowledgeDataObject> pageKnowledge(@Param("criteria") AdminAiKnowledgeCriteria criteria);

    long countKnowledge(@Param("criteria") AdminAiKnowledgeCriteria criteria);

    AdminAiKnowledgeDataObject findKnowledgeById(@Param("id") Long id);

    int insertKnowledge(AdminAiKnowledgeDataObject knowledge);

    int updateKnowledge(AdminAiKnowledgeDataObject knowledge);

    int deleteKnowledge(@Param("id") Long id);

    List<AdminAiKnowledgeDataObject> retrieveKnowledge(@Param("keyword") String keyword, @Param("limit") int limit);

    List<AdminAiOpsSuggestionRow> listSuggestions(@Param("limit") int limit);

    AdminAiOpsChatSessionDataObject findChatSessionById(@Param("id") Long id, @Param("adminUserId") Long adminUserId);

    AdminAiOpsChatSessionDataObject findLatestChatSession(@Param("adminUserId") Long adminUserId);

    int insertChatSession(AdminAiOpsChatSessionDataObject session);

    int touchChatSession(@Param("id") Long id);

    int insertChatMessage(AdminAiOpsChatMessageDataObject message);

    List<AdminAiOpsChatMessageDataObject> listChatMessages(@Param("sessionId") Long sessionId, @Param("limit") int limit);

    AdminAiOpsSuggestionActionDataObject findSuggestionAction(@Param("suggestionId") String suggestionId);

    int upsertSuggestionAction(AdminAiOpsSuggestionActionDataObject action);
}
