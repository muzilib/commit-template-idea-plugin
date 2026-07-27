package com.c301.plugin.config;

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
import java.util.List;

/**
 * AI 提交建议的全局非敏感配置。
 * API Key 只允许由 Password Safe 保存，不能添加到此状态对象或任何项目级配置中。
 */
@Data
@NoArgsConstructor
@State(name = "CommitTemplateAiPreferences", storages = @Storage("commit-template-ai.xml"))
public class AiPreferencesState implements PersistentStateComponent<AiPreferencesState> {
    private boolean enabled;
    private String endpoint = "https://api.openai.com/v1";
    private String apiPath = "/chat/completions";
    private String model = "gpt-4.1-mini";
    private double temperature = 0.2D;
    private int maxTokens = 1024;
    private boolean allowDiffTransfer;
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
    }
}
