package com.shelfflow.services.admin.aiops.domain;

import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiKnowledgeDataObject;
import com.shelfflow.services.admin.aiops.persistence.dataobject.AdminAiOpsSuggestionRow;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;

@Component
public class AdminAiOpsPolicy {

    private static final String DEFAULT_CATEGORY = "运营知识";
    private static final int PREVIEW_LENGTH = 180;
    private static final int PROMPT_CONTENT_PREVIEW_LENGTH = 600;

    public String normalizeTitle(String title) {
        return title == null ? "" : title.trim();
    }

    public String normalizeCategory(String category) {
        String normalized = category == null ? "" : category.trim();
        return normalized.isEmpty() ? DEFAULT_CATEGORY : normalized;
    }

    public String normalizeContent(String content) {
        return content == null ? "" : content.trim();
    }

    public String buildAnswer(String question,
                              List<AdminAiKnowledgeDataObject> knowledge,
                              List<AdminAiOpsSuggestionRow> suggestions) {
        StringJoiner answer = new StringJoiner("\n\n");
        answer.add("我基于当前知识库和实时运营数据给出以下建议：");

        if (!suggestions.isEmpty()) {
            StringJoiner suggestionLines = new StringJoiner("\n");
            for (AdminAiOpsSuggestionRow suggestion : suggestions) {
                suggestionLines.add("- " + suggestion.getTitle() + "：" + suggestion.getSuggestedAction());
            }
            answer.add("当前优先关注：\n" + suggestionLines);
        }

        if (!knowledge.isEmpty()) {
            StringJoiner knowledgeLines = new StringJoiner("\n");
            for (AdminAiKnowledgeDataObject item : knowledge) {
                knowledgeLines.add("- " + item.getTitle() + "：" + preview(item.getContent()));
            }
            answer.add("可参考知识：\n" + knowledgeLines);
        }

        if (suggestions.isEmpty() && knowledge.isEmpty()) {
            answer.add("暂未匹配到直接知识或高优先级批次。建议先查看损耗统计和定价规则模块，确认是否存在临期库存、售罄批次或高库存批次。");
        }

        answer.add("问题：" + question.trim());
        return answer.toString();
    }

    public String buildSystemPrompt() {
        return """
                你是 ShelfFlow 临期库存管理系统的 AI 运营助手。
                你的职责是帮助运营人员分析临期批次、损耗风险、定价策略、履约优先级和经营改进动作。
                回答必须使用中文，结论要具体、可执行，优先引用系统提供的实时运营数据和知识库。
                如果信息不足，要明确说明缺少哪些数据，并给出下一步排查路径。
                不要编造系统中没有给出的商品、批次、订单或财务数据。
                """;
    }

    public String buildUserPrompt(String question,
                                  List<AdminAiKnowledgeDataObject> knowledge,
                                  List<AdminAiOpsSuggestionRow> suggestions) {
        StringJoiner prompt = new StringJoiner("\n\n");
        prompt.add("用户问题：\n" + normalizeQuestion(question));

        if (!suggestions.isEmpty()) {
            StringJoiner suggestionLines = new StringJoiner("\n");
            for (AdminAiOpsSuggestionRow suggestion : suggestions) {
                suggestionLines.add("- [" + suggestion.getPriority() + "] "
                        + suggestion.getTitle()
                        + "，商品：" + suggestion.getProductName()
                        + "，批次：" + suggestion.getBatchCode()
                        + "，剩余天数：" + suggestion.getDaysToExpire()
                        + "，可用库存：" + suggestion.getAvailableQuantity()
                        + "，建议动作：" + suggestion.getSuggestedAction());
            }
            prompt.add("实时运营建议：\n" + suggestionLines);
        }

        if (!knowledge.isEmpty()) {
            StringJoiner knowledgeLines = new StringJoiner("\n");
            for (AdminAiKnowledgeDataObject item : knowledge) {
                knowledgeLines.add("- 标题：" + item.getTitle()
                        + "；分类：" + item.getCategory()
                        + "；内容：" + preview(item.getContent(), PROMPT_CONTENT_PREVIEW_LENGTH));
            }
            prompt.add("可引用知识库：\n" + knowledgeLines);
        }

        prompt.add("请输出：1. 关键判断；2. 推荐动作；3. 风险提醒。");
        return prompt.toString();
    }

    public List<String> references(List<AdminAiKnowledgeDataObject> knowledge, List<AdminAiOpsSuggestionRow> suggestions) {
        return java.util.stream.Stream.concat(
                knowledge.stream().map(item -> "知识库：" + item.getTitle()),
                suggestions.stream().map(item -> "运营建议：" + item.getTitle())
        ).toList();
    }

    private String preview(String content) {
        return preview(content, PREVIEW_LENGTH);
    }

    private String preview(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content == null ? "" : content;
        }
        return content.substring(0, maxLength) + "...";
    }

    private String normalizeQuestion(String question) {
        return question == null ? "" : question.trim();
    }
}
