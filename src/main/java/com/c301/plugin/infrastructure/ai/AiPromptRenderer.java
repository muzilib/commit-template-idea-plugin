package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.model.CommitTypeDomain;

import java.util.stream.Collectors;

/** 只构造结构化提交建议提示词，不包含密钥或任何本地路径以外的隐藏信息。 */
public final class AiPromptRenderer {
    private AiPromptRenderer() {
    }

    public static String systemPrompt(AiGenerationRequest request) {
        String types = request.allowedCommitTypes().stream()
                .map(CommitTypeDomain::getType)
                .collect(Collectors.joining(", "));
        return "你是 Git Commit 助手。请仅返回一个 JSON 对象，不要 Markdown、代码块或解释。"
                + "提交内容语言为 " + request.contentLanguage().getKey() + "。"
                + "type 必须是以下值之一：" + types + "。"
                + "subject 不能为空且不超过 " + request.rules().subjectMaxLength() + " 个字符。"
                + "JSON 字段固定为 type、scope、subject、body、breakingChange、issueNumbers。"
                + "body 与 breakingChange 可为 null，issueNumbers 必须为数字数组。";
    }

    public static String userPrompt(AiGenerationRequest request) {
        String title = request.transferMode().name().equals("DIFF") ? "已审核的变更 Diff：" : "已审核的变更元数据：";
        return title + "\n" + request.sanitizedChangeContent();
    }
}
