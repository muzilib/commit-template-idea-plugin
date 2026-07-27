package com.c301.plugin.domain.ai;

/**
 * 面向用户展示的 AI 请求失败信息；不得包含 API Key、Authorization、完整 Prompt 或 Diff。
 */
public record AiGenerationError(Kind kind, String message) {
    public enum Kind {
        CONFIGURATION,
        CANCELED,
        NETWORK,
        PROVIDER,
        RESPONSE
    }
}
