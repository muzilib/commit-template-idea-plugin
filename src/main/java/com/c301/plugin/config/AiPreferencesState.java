package com.c301.plugin.config;

import com.c301.plugin.domain.ai.AiDataTransferConsent;
import com.c301.plugin.domain.ai.AiProviderType;
import com.c301.plugin.domain.ai.QwenGenerationOptions;
import com.c301.plugin.infrastructure.credentials.PasswordSafeAiCredentialStore;
import com.c301.plugin.ui.PluginNotifications;
import com.c301.plugin.utils.CommUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 提交建议的全局非敏感配置。
 * API Key 只允许由 Password Safe 保存，不能添加到此状态对象或任何项目级配置中。
 */
@Data
@NoArgsConstructor
@State(name = "CommitTemplateAiPreferences", storages = @Storage("commit-template-ai.xml"))
public class AiPreferencesState implements PersistentStateComponent<AiPreferencesState> {


    private static final int CURRENT_MIGRATION_VERSION = 1;

    private boolean enabled;
    private int migrationVersion;
    private transient boolean credentialMigrationScheduled;
    private AiProviderType providerType = AiProviderType.QWEN;
    private String apiUrl = AiProviderType.QWEN.apiUrl();
    private String model = "qwen3.7-max";
    /**
     * 兼容旧版配置，升级后会将 endpoint 与 apiPath 合并为 apiUrl。
     */
    @Deprecated
    private String endpoint;
    @Deprecated
    private String apiPath;
    private Map<AiProviderType, String> customSystemPrompts = new LinkedHashMap<>();
    private double temperature = 0.7D;
    private int maxTokens = 1024;
    private QwenGenerationOptions qwenGenerationOptions = new QwenGenerationOptions();

    private AiDataTransferConsent dataTransferConsent = AiDataTransferConsent.UNDECIDED;
    private boolean checkDiffBeforeSending = false;
    private boolean showAdvancedSettings;
    private List<String> excludePatterns = new ArrayList<>(List.of(
            ".env", ".env.*", "*.pem", "*.key", "*.p12", "*.jks",
            "id_rsa", "id_ed25519", "**/secrets/**", "**/credentials/**",
            "node_modules/", "build/", "out/", "target/", "dist/"
    ));

    public static AiPreferencesState getInstance() {
        return ApplicationManager.getApplication().getService(AiPreferencesState.class);
    }

    private static String joinLegacyApiUrl(String legacyEndpoint, String legacyApiPath) {
        if (legacyEndpoint == null || legacyEndpoint.isBlank()) {
            return "";
        }
        String base = legacyEndpoint.trim();
        String path = legacyApiPath == null || legacyApiPath.isBlank() ? "/chat/completions" : legacyApiPath.trim();
        if (base.endsWith("/") && path.startsWith("/")) {
            return base + path.substring(1);
        }
        if (!base.endsWith("/") && !path.startsWith("/")) {
            return base + "/" + path;
        }
        return base + path;
    }

    private static boolean isSupportedApiUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.startsWith("https://") || normalized.startsWith("http://localhost")
                || normalized.startsWith("http://127.0.0.1");
    }

    private static AiProviderType providerForApiUrl(String value, AiProviderType fallback) {
        String normalized = value == null ? "" : value.trim();
        if (AiProviderType.QWEN.apiUrl().equals(normalized)) {
            return AiProviderType.QWEN;
        }
        if (AiProviderType.CHATGPT.apiUrl().equals(normalized)) {
            return AiProviderType.CHATGPT;
        }
        if (AiProviderType.DEEPSEEK.apiUrl().equals(normalized)) {
            return AiProviderType.DEEPSEEK;
        }
        return fallback == null ? AiProviderType.CUSTOM : fallback;
    }

    /**
     * 仅清理可明确判定为无效的 AI 非敏感配置，避免升级时覆盖有效用户偏好。
     */
    private void sanitizePreferences() {
        if (!isSupportedApiUrl(apiUrl)) {
            apiUrl = providerType.usesPresetApiUrl() ? providerType.apiUrl() : "";
        } else {
            apiUrl = apiUrl.trim();
        }
        if (model == null) {
            model = "";
        } else {
            model = model.trim();
        }
        if (Double.isNaN(temperature) || Double.isInfinite(temperature) || temperature < 0D || temperature > 2D) {
            temperature = 0.7D;
        }
        if (maxTokens < 1 || maxTokens > 16_384) {
            maxTokens = 1024;
        }
        excludePatterns = new ArrayList<>(excludePatterns.stream().filter(value -> value != null && !value.isBlank())
                .map(String::trim).distinct().toList());
        customSystemPrompts.entrySet().removeIf(entry -> entry.getKey() == null || entry.getValue() == null
                || entry.getValue().isBlank());
    }

    private boolean migrateCredentials(String legacyApiUrl, AiProviderType provider, String configuredApiUrl) {
        PasswordSafeAiCredentialStore credentialStore = new PasswordSafeAiCredentialStore();
        boolean migrated = true;
        migrated &= credentialStore.migrateLegacyApiKey(AiProviderType.QWEN, AiProviderType.QWEN.apiUrl());
        migrated &= credentialStore.migrateLegacyApiKey(AiProviderType.CHATGPT, AiProviderType.CHATGPT.apiUrl());
        migrated &= credentialStore.migrateLegacyApiKey(AiProviderType.DEEPSEEK, AiProviderType.DEEPSEEK.apiUrl());
        migrated &= credentialStore.migrateLegacyApiKey(provider, legacyApiUrl, configuredApiUrl);
        return migrated;
    }

    private void migrateLegacyPreferences() {
        String legacyApiUrl = joinLegacyApiUrl(endpoint, apiPath);
        if (!legacyApiUrl.isBlank() && (apiUrl == null || apiUrl.isBlank()
                || AiProviderType.QWEN.apiUrl().equals(apiUrl))) {
            apiUrl = legacyApiUrl;
        }
        providerType = legacyApiUrl.isBlank() ? providerForApiUrl(apiUrl, providerType)
                : providerForApiUrl(legacyApiUrl, AiProviderType.CUSTOM);
        sanitizePreferences();
        endpoint = null;
        apiPath = null;
        scheduleCredentialMigration(legacyApiUrl, providerType, apiUrl);
    }

    /**
     * Password Safe 可能访问系统钥匙串，禁止在 EDT 中读写。
     */
    private void scheduleCredentialMigration(String legacyApiUrl, AiProviderType provider, String configuredApiUrl) {
        if (credentialMigrationScheduled) {
            return;
        }
        credentialMigrationScheduled = true;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            boolean migrated = migrateCredentials(legacyApiUrl, provider, configuredApiUrl);
            ApplicationManager.getApplication().invokeLater(() -> {
                migrationVersion = CURRENT_MIGRATION_VERSION;
                credentialMigrationScheduled = false;
                if (!migrated) {
                    PluginNotifications.notify(null, CommUtil.i18nResourceBundle(null)
                            .getString("plugin.ai.credentialMigrationFailed"), NotificationType.WARNING);
                }
            });
        });
    }

    /**
     * 重置时不需要尝试访问或迁移旧凭据，直接恢复非敏感配置默认值。
     */
    public void resetToDefaults() {
        XmlSerializerUtil.copyBean(new AiPreferencesState(), this);
        migrationVersion = CURRENT_MIGRATION_VERSION;
        credentialMigrationScheduled = false;
    }

    @Override
    public @Nullable AiPreferencesState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AiPreferencesState state) {
        XmlSerializerUtil.copyBean(state, this);
        if (excludePatterns == null) {
            excludePatterns = new ArrayList<>();
        }
        if (providerType == null) {
            providerType = AiProviderType.QWEN;
        }
        if (customSystemPrompts == null) {
            customSystemPrompts = new LinkedHashMap<>();
        }
        if (qwenGenerationOptions == null) {
            qwenGenerationOptions = new QwenGenerationOptions();
        }
        if (dataTransferConsent == null) {
            dataTransferConsent = AiDataTransferConsent.UNDECIDED;
        }
        if (migrationVersion < CURRENT_MIGRATION_VERSION) {
            migrateLegacyPreferences();
        } else {
            sanitizePreferences();
        }
    }
}
