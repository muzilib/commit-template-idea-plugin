package com.c301.plugin.domain.ai;

/**
 * AI 服务商预设。预设服务商固定兼容接口地址，模型名称始终由用户填写。
 */
public enum AiProviderType {
    QWEN("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
            "qwen3.7-max",
            "qwen",
            "plugin.ai.provider.qwen",
            "plugin.ai.modelPlaceholder.qwen",
            "plugin.ai.provider.qwen.apiKeyHelp.message",
            "plugin.ai.provider.qwen.apiKeyHelp.link",
            "https://platform.qianwenai.com/home/api-keys"),
    CHATGPT("https://api.openai.com/v1/responses",
            "gpt-5.6",
            "chatgpt",
            "plugin.ai.provider.chatgpt",
            "plugin.ai.modelPlaceholder.chatgpt",
            "plugin.ai.provider.chatgpt.apiKeyHelp.message",
            "plugin.ai.provider.chatgpt.apiKeyHelp.link",
            "https://platform.openai.com/api-keys"),
    DEEPSEEK("https://api.deepseek.com/chat/completions",
            "deepseek-v4-flash",
            "deepseek",
            "plugin.ai.provider.deepseek",
            "plugin.ai.modelPlaceholder.deepseek",
            "plugin.ai.provider.deepseek.apiKeyHelp.message",
            "plugin.ai.provider.deepseek.apiKeyHelp.link",
            "https://platform.deepseek.com/api_keys"),
    CUSTOM("",
            "",
            "common",
            "plugin.ai.provider.custom",
            "plugin.ai.modelPlaceholder.custom",
            "plugin.ai.provider.custom.apiKeyHelp.message",
            null,
            null);

    private final String apiUrl;
    private final String defaultModel;
    private final String promptTemplate;
    private final String displayNameKey;
    private final String modelPlaceholderKey;
    private final String apiKeyHelpMessageKey;
    private final String apiKeyHelpLinkTextKey;
    private final String apiKeyHelpUrl;

    AiProviderType(String apiUrl, String defaultModel, String promptTemplate, String displayNameKey,
                   String modelPlaceholderKey, String apiKeyHelpMessageKey, String apiKeyHelpLinkTextKey,
                   String apiKeyHelpUrl) {
        this.apiUrl = apiUrl;
        this.defaultModel = defaultModel;
        this.promptTemplate = promptTemplate;
        this.displayNameKey = displayNameKey;
        this.modelPlaceholderKey = modelPlaceholderKey;
        this.apiKeyHelpMessageKey = apiKeyHelpMessageKey;
        this.apiKeyHelpLinkTextKey = apiKeyHelpLinkTextKey;
        this.apiKeyHelpUrl = apiKeyHelpUrl;
    }

    public String apiUrl() {
        return apiUrl;
    }

    public String defaultModel() {
        return defaultModel;
    }

    public String promptTemplate() {
        return promptTemplate;
    }

    public String displayNameKey() {
        return displayNameKey;
    }

    public String modelPlaceholderKey() {
        return modelPlaceholderKey;
    }

    public String apiKeyHelpMessageKey() {
        return apiKeyHelpMessageKey;
    }

    public String apiKeyHelpLinkTextKey() {
        return apiKeyHelpLinkTextKey;
    }

    public String apiKeyHelpUrl() {
        return apiKeyHelpUrl;
    }

    public boolean hasApiKeyHelp() {
        return apiKeyHelpMessageKey != null && !apiKeyHelpMessageKey.isBlank();
    }

    public boolean hasApiKeyHelpLink() {
        return apiKeyHelpLinkTextKey != null && !apiKeyHelpLinkTextKey.isBlank()
                && apiKeyHelpUrl != null && !apiKeyHelpUrl.isBlank();
    }

    public boolean usesPresetApiUrl() {
        return this != CUSTOM;
    }
}
