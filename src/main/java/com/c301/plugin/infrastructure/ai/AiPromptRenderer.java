package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.model.CommitTypeDomain;

import java.util.stream.Collectors;

/**
 * 只构造结构化提交建议提示词，不包含密钥或任何本地路径以外的隐藏信息。
 */
public final class AiPromptRenderer {
    private AiPromptRenderer() {
    }

    public static String systemPrompt(AiGenerationRequest request) {
        String types = request.allowedCommitTypes().stream()
                .map(CommitTypeDomain::getType)
                .collect(Collectors.joining(", "));
        String template = request.systemPromptTemplate();
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("未配置 AI 系统提示词。");
        }
        return template
                .replace("{languageLabel}", request.contentLanguage().getLabel())
                .replace("{languageKey}", request.contentLanguage().getKey())
                .replace("{allowedTypes}", types)
                .replace("{subjectMaxLength}", String.valueOf(request.rules().subjectMaxLength()));
    }

    public static String userPrompt(AiGenerationRequest request) {
        String title = request.transferMode().name().equals("DIFF") ? "已审核的变更 Diff：" : "已审核的变更元数据：";
        return title + "\n" + request.sanitizedChangeContent();
    }
}
