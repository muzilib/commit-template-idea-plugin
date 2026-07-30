package com.c301.plugin.domain.ai;

/**
 * AI 服务商预设。预设服务商固定兼容接口地址，模型名称始终由用户填写。
 */
public enum AiProviderType {
    QWEN("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", "qwen", "plugin.ai.modelPlaceholder.qwen"),
    CHATGPT("https://api.openai.com/v1/chat/completions", "chatgpt", "plugin.ai.modelPlaceholder.chatgpt"),
    DEEPSEEK("https://api.deepseek.com/chat/completions", "deepseek", "plugin.ai.modelPlaceholder.deepseek"),
    CUSTOM("", "common", "plugin.ai.modelPlaceholder.custom");

    private final String apiUrl;
    private final String promptTemplate;
    private final String modelPlaceholderKey;

    AiProviderType(String apiUrl, String promptTemplate, String modelPlaceholderKey) {
        this.apiUrl = apiUrl;
        this.promptTemplate = promptTemplate;
        this.modelPlaceholderKey = modelPlaceholderKey;
    }

    public String apiUrl() {
        return apiUrl;
    }

    public String promptTemplate() {
        return promptTemplate;
    }

    public String modelPlaceholderKey() {
        return modelPlaceholderKey;
    }

    public boolean usesPresetApiUrl() {
        return this != CUSTOM;
    }
}
