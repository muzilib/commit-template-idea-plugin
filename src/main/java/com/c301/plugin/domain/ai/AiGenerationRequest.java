package com.c301.plugin.domain.ai;

import com.c301.plugin.domain.commit.CommitMessageRules;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.LanguageDomain;

import java.util.List;

/**
 * 已通过本地安全策略审核后，允许交给 Provider 的请求数据。
 */
public record AiGenerationRequest(
        String apiUrl,
        String model,
        String systemPromptTemplate,
        double temperature,
        int maxTokens,
        QwenGenerationOptions qwenGenerationOptions,
        LanguageDomain contentLanguage,
        List<CommitTypeDomain> allowedCommitTypes,
        CommitMessageRules rules,
        String commitTemplateContext,
        String sanitizedDiff
) {
}
