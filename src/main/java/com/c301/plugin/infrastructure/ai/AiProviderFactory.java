package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiProvider;
import com.c301.plugin.domain.ai.AiProviderType;

/**
 * 根据用户选择创建对应服务商，避免界面层依赖具体 Provider 实现。
 */
public final class AiProviderFactory {
    private AiProviderFactory() {
    }

    public static AiProvider create(AiProviderType providerType) {
        return switch (providerType == null ? AiProviderType.CUSTOM : providerType) {
            case QWEN -> new QwenAiProvider();
            case CHATGPT -> new ChatGptAiProvider();
            case DEEPSEEK -> new DeepSeekAiProvider();
            case CUSTOM -> new CustomAiProvider();
        };
    }
}
