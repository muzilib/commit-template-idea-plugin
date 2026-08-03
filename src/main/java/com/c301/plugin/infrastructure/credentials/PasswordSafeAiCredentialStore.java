package com.c301.plugin.infrastructure.credentials;

import com.c301.plugin.domain.ai.AiProviderType;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * IntelliJ Password Safe 适配器。每个 AI 供应商独立保存一份密钥，明文不会进入 State。
 */
public final class PasswordSafeAiCredentialStore implements AiCredentialStore {
    private static final String SERVICE_NAME = "commit-template-ai";
    private static final String PROVIDER_ACCOUNT_PREFIX = "provider:";

    private static String legacyEndpoint(String apiUrl) {
        if (apiUrl == null) {
            return null;
        }
        String value = apiUrl.trim();
        String suffix = "/chat/completions";
        return value.endsWith(suffix) ? value.substring(0, value.length() - suffix.length()) : null;
    }

    private CredentialAttributes providerAttributes(AiProviderType providerType) {
        AiProviderType provider = providerType == null ? AiProviderType.CUSTOM : providerType;
        return attributes(PROVIDER_ACCOUNT_PREFIX + provider.name().toLowerCase(Locale.ROOT));
    }

    private CredentialAttributes attributes(String accountName) {
        String normalized = accountName == null ? "default" : accountName.trim().toLowerCase(Locale.ROOT);
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName(SERVICE_NAME, normalized));
    }

    private String read(CredentialAttributes attributes) {
        try {
            Credentials credentials = PasswordSafe.getInstance().get(attributes);
            return credentials == null ? null : credentials.getPasswordAsString();
        } catch (RuntimeException ignored) {
            // macOS Keychain 暂时不可用或条目异常时，不能阻断设置页和 AI 功能的正常交互。
            return null;
        }
    }

    @Override
    public boolean hasCredential(AiProviderType providerType) {
        String key = readApiKey(providerType);
        return key != null && !key.isBlank();
    }

    @Override
    public String readApiKey(AiProviderType providerType) {
        return read(providerAttributes(providerType));
    }

    @Override
    public void saveApiKey(AiProviderType providerType, String apiKey) {
        PasswordSafe.getInstance().set(providerAttributes(providerType), new Credentials(null, apiKey));
    }

    @Override
    public void clearApiKey(AiProviderType providerType) {
        PasswordSafe.getInstance().set(providerAttributes(providerType), null);
    }

    @Override
    public void clearAllApiKeys() {
        for (AiProviderType providerType : AiProviderType.values()) {
            clearApiKey(providerType);
        }
    }

    @Override
    public boolean migrateLegacyApiKey(AiProviderType providerType, String... legacyApiUrls) {
        Set<String> legacyAccounts = new LinkedHashSet<>();
        if (legacyApiUrls != null) {
            for (String apiUrl : legacyApiUrls) {
                if (apiUrl == null || apiUrl.isBlank()) {
                    continue;
                }
                legacyAccounts.add(apiUrl.trim());
                String endpoint = legacyEndpoint(apiUrl);
                if (endpoint != null && !endpoint.isBlank()) {
                    legacyAccounts.add(endpoint);
                }
            }
        }
        if (legacyAccounts.isEmpty()) {
            return true;
        }

        boolean succeeded = true;
        try {
            String providerKey = readApiKey(providerType);
            if (providerKey == null || providerKey.isBlank()) {
                for (String account : legacyAccounts) {
                    String legacyKey = read(attributes(account));
                    if (legacyKey != null && !legacyKey.isBlank()) {
                        saveApiKey(providerType, legacyKey);
                        break;
                    }
                }
            }
        } catch (RuntimeException ignored) {
            succeeded = false;
        } finally {
            for (String account : legacyAccounts) {
                try {
                    PasswordSafe.getInstance().set(attributes(account), null);
                } catch (RuntimeException ignored) {
                    succeeded = false;
                }
            }
        }
        return succeeded;
    }
}
