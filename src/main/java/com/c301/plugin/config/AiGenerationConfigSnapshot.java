package com.c301.plugin.config;

import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.domain.ai.AiProviderType;
import com.c301.plugin.domain.ai.DeepSeekGenerationOptions;
import com.c301.plugin.domain.ai.OpenAiGenerationOptions;
import com.c301.plugin.domain.ai.QwenGenerationOptions;
import com.c301.plugin.infrastructure.ai.AiCommitTemplateContextRenderer;
import com.c301.plugin.infrastructure.ai.AiSystemPromptTemplates;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitmojiDomain;
import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.utils.CommUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次 AI 生成操作使用的非敏感配置快照。
 * 设置页在生成期间应用的新配置只会影响下一次生成。
 */
public final class AiGenerationConfigSnapshot {
    private final AiProviderType providerType;
    private final String apiUrl;
    private final String model;
    private final String systemPrompt;
    private final double temperature;
    private final int maxTokens;
    private final QwenGenerationOptions qwenGenerationOptions;
    private final DeepSeekGenerationOptions deepSeekGenerationOptions;
    private final OpenAiGenerationOptions openAiGenerationOptions;
    private final EffectiveCommitTemplateSettings effectiveSettings;
    private final LanguageDomain generationLanguage;
    private final List<CommitTypeDomain> allowedTypes;
    private final String commitTemplateContext;
    private final boolean checkDiffBeforeSending;

    private AiGenerationConfigSnapshot(AiProviderType providerType, String apiUrl, String model, String systemPrompt,
                                       double temperature, int maxTokens, QwenGenerationOptions qwenGenerationOptions,
                                       DeepSeekGenerationOptions deepSeekGenerationOptions,
                                       OpenAiGenerationOptions openAiGenerationOptions,
                                       EffectiveCommitTemplateSettings effectiveSettings, LanguageDomain generationLanguage,
                                       List<CommitTypeDomain> allowedTypes, String commitTemplateContext,
                                       boolean checkDiffBeforeSending) {
        this.providerType = providerType;
        this.apiUrl = apiUrl;
        this.model = model;
        this.systemPrompt = systemPrompt;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.qwenGenerationOptions = qwenGenerationOptions;
        this.deepSeekGenerationOptions = deepSeekGenerationOptions;
        this.openAiGenerationOptions = openAiGenerationOptions;
        this.effectiveSettings = effectiveSettings;
        this.generationLanguage = generationLanguage;
        this.allowedTypes = allowedTypes;
        this.commitTemplateContext = commitTemplateContext;
        this.checkDiffBeforeSending = checkDiffBeforeSending;
    }

    public static AiGenerationConfigSnapshot capture(AiPreferencesState preferences,
                                                     EffectiveCommitTemplateSettings settings) {
        AiProviderType providerType = preferences.getProviderType() == null
                ? AiProviderType.CUSTOM : preferences.getProviderType();
        List<CommitTypeDomain> allowedTypes = copyCommitTypes(settings.customEnable()
                ? settings.customCommitTypeList()
                : CommUtil.getDefaultCommitTypeList(settings.language().getKey()));
        EffectiveCommitTemplateSettings copiedSettings = new EffectiveCommitTemplateSettings(
                settings.language(), settings.customEnable(), settings.emojiEnable(), settings.emojiLocation(),
                allowedTypes, settings.commitMessageRules(), settings.previewEnabled());
        LanguageDomain generationLanguage = preferences.getGenerationLanguage();
        String prompt = preferences.getCustomSystemPrompts().get(providerType);
        if (prompt == null || prompt.isBlank()) {
            prompt = AiSystemPromptTemplates.forProvider(providerType);
        }
        return new AiGenerationConfigSnapshot(providerType, preferences.getApiUrl(), preferences.getModel(), prompt,
                preferences.getTemperature(), preferences.getMaxTokens(),
                copyQwenOptions(preferences.getQwenGenerationOptions()),
                copyDeepSeekOptions(preferences.getDeepSeekGenerationOptions()),
                copyOpenAiOptions(preferences.getOpenAiGenerationOptions()), copiedSettings, generationLanguage, allowedTypes,
                AiCommitTemplateContextRenderer.render(copiedSettings, generationLanguage, allowedTypes),
                preferences.isCheckDiffBeforeSending());
    }

    public AiGenerationRequest createRequest(String sanitizedDiff) {
        return new AiGenerationRequest(apiUrl, model, systemPrompt, temperature, maxTokens,
                qwenGenerationOptions, deepSeekGenerationOptions, openAiGenerationOptions,
                generationLanguage, allowedTypes, effectiveSettings.commitMessageRules(),
                commitTemplateContext, sanitizedDiff);
    }

    public AiProviderType providerType() {
        return providerType;
    }

    public EffectiveCommitTemplateSettings effectiveSettings() {
        return effectiveSettings;
    }

    public List<CommitTypeDomain> allowedTypes() {
        return allowedTypes;
    }

    public boolean checkDiffBeforeSending() {
        return checkDiffBeforeSending;
    }

    public boolean isQwenDataInspectionEnabled() {
        return providerType == AiProviderType.QWEN && qwenGenerationOptions.isDataInspectionEnabled();
    }

    private static List<CommitTypeDomain> copyCommitTypes(List<CommitTypeDomain> source) {
        List<CommitTypeDomain> copied = new ArrayList<>();
        if (source != null) {
            for (CommitTypeDomain type : source) {
                if (type != null) {
                    copied.add(new CommitTypeDomain(type.getType(), copyGitmoji(type.getEmoji()), type.getDescription()));
                }
            }
        }
        return List.copyOf(copied);
    }

    private static GitmojiDomain copyGitmoji(GitmojiDomain source) {
        return source == null ? null : new GitmojiDomain(source.getCode(), source.getName(), source.getEmoji(),
                source.getDescription());
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
