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
        String scopeRequirement = request.rules().requireScope()
                ? "scope is REQUIRED and must be a non-empty string"
                : "scope is optional and may be null only when it cannot be reliably inferred";
        String trailingPeriodRequirement = request.rules().forbidSubjectTrailingPeriod()
                ? "a trailing period is forbidden"
                : "a trailing period is allowed by the local rule";
        String template = request.systemPromptTemplate();
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("未配置 AI 系统提示词。");
        }
        String renderedTemplate = template
                .replace("{languageLabel}", request.contentLanguage().getLabel())
                .replace("{languageKey}", request.contentLanguage().getKey())
                .replace("{allowedTypes}", types)
                .replace("{subjectMaxLength}", request.rules().subjectLengthLimitEnabled()
                        ? String.valueOf(request.rules().subjectMaxLength()) : "未启用")
                .replace("{scopeRequirement}", scopeRequirement)
                .replace("{trailingPeriodRequirement}", trailingPeriodRequirement);
        String lengthRule = request.rules().subjectLengthLimitEnabled()
                && request.rules().subjectMaxLength() > 0
                ? "subject 去除首尾空白后的长度不得超过 " + request.rules().subjectMaxLength()
                + " 个字符；若标题过长，必须先压缩措辞再输出。"
                : "项目未启用 subject 长度限制，不要因为默认经验值擅自截断标题。";
        return renderedTemplate + "\n\n最终输出前必须逐项确认：只返回指定的 JSON 对象；type 必须属于 ["
                + types + "]；" + lengthRule;
    }

    public static String userPrompt(AiGenerationRequest request) {
        return "当前提交模板规则：\n" + request.commitTemplateContext()
                + "\n\n待提交的已筛选 Diff：\n" + request.sanitizedDiff();
    }
}
