package com.c301.plugin.infrastructure.credentials;

import com.c301.plugin.domain.ai.AiProviderType;

/**
 * Password Safe 的 AI 凭据访问接口，便于替换和测试。
 * 每个供应商仅保存一份独立凭据，不依赖当前模型或 API 地址。
 */
public interface AiCredentialStore {
    boolean hasCredential(AiProviderType providerType);

    String readApiKey(AiProviderType providerType);

    void saveApiKey(AiProviderType providerType, String apiKey);

    void clearApiKey(AiProviderType providerType);

    void clearAllApiKeys();

    /**
     * 将旧版按 API 地址保存的密钥迁移为按供应商保存，并删除所有旧索引。
     * 返回 false 表示迁移过程中发生异常，调用方可提示用户重新设置密钥。
     */
    boolean migrateLegacyApiKey(AiProviderType providerType, String... legacyApiUrls);
}
