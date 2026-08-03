package com.c301.plugin.domain.ai;

import com.c301.plugin.domain.commit.CommitMessageRules;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitmojiDomain;
import com.c301.plugin.model.LanguageDomain;

import java.util.ArrayList;
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
        DeepSeekGenerationOptions deepSeekGenerationOptions,
        OpenAiGenerationOptions openAiGenerationOptions,
        LanguageDomain contentLanguage,
        List<CommitTypeDomain> allowedCommitTypes,
        CommitMessageRules rules,
        String commitTemplateContext,
        String sanitizedDiff
) {
    public AiGenerationRequest {
        qwenGenerationOptions = copyQwenOptions(qwenGenerationOptions);
        deepSeekGenerationOptions = copyDeepSeekOptions(deepSeekGenerationOptions);
        openAiGenerationOptions = copyOpenAiOptions(openAiGenerationOptions);
        allowedCommitTypes = copyCommitTypes(allowedCommitTypes);
    }

    private static List<CommitTypeDomain> copyCommitTypes(List<CommitTypeDomain> source) {
        List<CommitTypeDomain> copied = new ArrayList<>();
        if (source != null) {
            for (CommitTypeDomain type : source) {
                if (type != null) {
                    GitmojiDomain emoji = type.getEmoji();
                    GitmojiDomain copiedEmoji = emoji == null ? null : new GitmojiDomain(emoji.getCode(), emoji.getName(),
                            emoji.getEmoji(), emoji.getDescription());
                    copied.add(new CommitTypeDomain(type.getType(), copiedEmoji, type.getDescription()));
                }
            }
        }
        return List.copyOf(copied);
    }

    private static QwenGenerationOptions copyQwenOptions(QwenGenerationOptions source) {
        QwenGenerationOptions copied = new QwenGenerationOptions();
        if (source == null) {
            return copied;
        }
        copied.setIncludeUsage(source.isIncludeUsage());
        copied.setTopP(source.getTopP());
        copied.setTopK(source.getTopK());
        copied.setRepetitionPenalty(source.getRepetitionPenalty());
        copied.setPresencePenalty(source.getPresencePenalty());
        copied.setSeed(source.getSeed());
        copied.setEnableThinking(source.isEnableThinking());
        copied.setThinkingBudget(source.getThinkingBudget());
        copied.setReasoningEffort(source.getReasoningEffort());
        copied.setEnableSearch(source.isEnableSearch());
        copied.setForceSearch(source.isForceSearch());
        copied.setSearchStrategy(source.getSearchStrategy());
        copied.setDataInspectionEnabled(source.isDataInspectionEnabled());
        return copied;
    }

    private static DeepSeekGenerationOptions copyDeepSeekOptions(DeepSeekGenerationOptions source) {
        DeepSeekGenerationOptions copied = new DeepSeekGenerationOptions();
        if (source == null) {
            return copied;
        }
        copied.setIncludeUsage(source.isIncludeUsage());
        copied.setTopP(source.getTopP());
        copied.setEnableThinking(source.isEnableThinking());
        copied.setReasoningEffort(source.getReasoningEffort());
        return copied;
    }

    private static OpenAiGenerationOptions copyOpenAiOptions(OpenAiGenerationOptions source) {
        OpenAiGenerationOptions copied = new OpenAiGenerationOptions();
        if (source == null) {
            return copied;
        }
        copied.setReasoningEffort(source.getReasoningEffort());
        copied.setVerbosity(source.getVerbosity());
        copied.setTopP(source.getTopP());
        copied.setStoreResponse(source.isStoreResponse());
        return copied;
    }
}
