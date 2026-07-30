package com.c301.plugin.infrastructure.credentials;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

/**
 * IntelliJ Password Safe 适配器。Endpoint 用于隔离不同服务的密钥，明文不会进入 State。
 */
public final class PasswordSafeAiCredentialStore implements AiCredentialStore {
    private static final String SERVICE_NAME = "commit-template-ai";

    private CredentialAttributes attributes(String endpoint) {
        String accountName = endpoint == null ? "default" : endpoint.trim().toLowerCase();
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName(SERVICE_NAME, accountName));
    }

    @Override
    public boolean hasCredential(String endpoint) {
        String key = readApiKey(endpoint);
        return key != null && !key.isBlank();
    }

    @Override
    public String readApiKey(String endpoint) {
        Credentials credentials = PasswordSafe.getInstance().get(attributes(endpoint));
        if (credentials != null && credentials.getPasswordAsString() != null) {
            return credentials.getPasswordAsString();
        }
        // 兼容旧版以服务地址（不含 /chat/completions）索引保存的 API Key。
        String legacyEndpoint = legacyEndpoint(endpoint);
        if (legacyEndpoint == null) {
            return null;
        }
        Credentials legacyCredentials = PasswordSafe.getInstance().get(attributes(legacyEndpoint));
        return legacyCredentials == null ? null : legacyCredentials.getPasswordAsString();
    }

    private static String legacyEndpoint(String apiUrl) {
        if (apiUrl == null) {
            return null;
        }
        String value = apiUrl.trim();
        String suffix = "/chat/completions";
        return value.endsWith(suffix) ? value.substring(0, value.length() - suffix.length()) : null;
    }

    @Override
    public void saveApiKey(String endpoint, String apiKey) {
        PasswordSafe.getInstance().set(attributes(endpoint), new Credentials(null, apiKey));
    }

    @Override
    public void clearApiKey(String endpoint) {
        PasswordSafe.getInstance().set(attributes(endpoint), null);
    }
}
