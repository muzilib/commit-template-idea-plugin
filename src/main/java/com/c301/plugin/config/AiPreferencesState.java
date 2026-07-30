package com.c301.plugin.config;

import com.c301.plugin.domain.ai.AiDataTransferConsent;
import com.c301.plugin.domain.ai.AiProviderType;
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


    private boolean enabled;
    private AiProviderType providerType = AiProviderType.QWEN;
    private String apiUrl = AiProviderType.QWEN.apiUrl();
    private String model = "qwen3.7-max";
    /** 兼容旧版配置，升级后会将 endpoint 与 apiPath 合并为 apiUrl。 */
    @Deprecated
    private String endpoint;
    @Deprecated
    private String apiPath;
    private Map<AiProviderType, String> customSystemPrompts = new LinkedHashMap<>();
    private double temperature = 0.7D;
    private int maxTokens = 1024;

    private AiDataTransferConsent dataTransferConsent = AiDataTransferConsent.UNDECIDED;
    private boolean checkDiffBeforeSending = true;
    private boolean showAdvancedSettings;
    private List<String> excludePatterns = new ArrayList<>(List.of(
            ".env", ".env.*", "*.pem", "*.key", "*.p12", "*.jks",
            "id_rsa", "id_ed25519", "**/secrets/**", "**/credentials/**",
            "node_modules/", "build/", "out/", "target/", "dist/"
    ));

    public static AiPreferencesState getInstance() {
        return ApplicationManager.getApplication().getService(AiPreferencesState.class);
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
        String migratedLegacyUrl = joinLegacyApiUrl(endpoint, apiPath);
        if (!migratedLegacyUrl.isBlank() && !migratedLegacyUrl.equals(AiProviderType.QWEN.apiUrl())) {
            providerType = AiProviderType.CUSTOM;
        }
        if (!migratedLegacyUrl.isBlank() && (apiUrl == null || apiUrl.isBlank()
                || (providerType == AiProviderType.QWEN && apiUrl.equals(AiProviderType.QWEN.apiUrl())))) {
            apiUrl = migratedLegacyUrl;
        }
        if (apiUrl == null || apiUrl.isBlank()) {
            apiUrl = providerType.usesPresetApiUrl() ? providerType.apiUrl() : "";
        }
        if (customSystemPrompts == null) {
            customSystemPrompts = new LinkedHashMap<>();
        }
        if (dataTransferConsent == null) {
            dataTransferConsent = AiDataTransferConsent.UNDECIDED;
        }
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
}
