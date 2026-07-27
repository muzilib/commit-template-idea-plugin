package com.c301.plugin.infrastructure.credentials;

/** Password Safe 的 AI 凭据访问接口，便于替换和测试。 */
public interface AiCredentialStore {
    boolean hasCredential(String endpoint);

    String readApiKey(String endpoint);

    void saveApiKey(String endpoint, String apiKey);

    void clearApiKey(String endpoint);
}
