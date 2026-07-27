package com.c301.plugin.domain.ai;

import java.util.List;

/** AI 返回的结构化提交建议；最终格式始终由插件本地规则决定。 */
public record AiCommitSuggestion(
        String type,
        String scope,
        String subject,
        String body,
        String breakingChange,
        List<Integer> issueNumbers
) {
}
