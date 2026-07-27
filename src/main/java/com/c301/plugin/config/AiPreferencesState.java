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
    public static final String DEFAULT_SYSTEM_PROMPT = """
            你是严格遵循 Conventional Commits 的 Git 提交信息助手。
            只输出一个合法 JSON 对象；不得输出 Markdown、代码块、思考过程、解释或其他文本。
            所有自然语言字段必须使用 {languageLabel}（语言代码 {languageKey}）。
            仅可根据用户提供的变更信息生成建议；不得根据文件名、路径或常识虚构未提供的功能、修复、重构、Issue 或破坏性变更。
            当信息仅为文件元数据且不足以判断具体内容时，subject 必须使用保守、概括的描述，body、breakingChange 设为 null，issueNumbers 设为 []。
            type 必须是以下值之一：{allowedTypes}。
            scope 是可选的简短模块名；无法可靠判断时设为 null。
            subject 必须是简洁的变更说明、不能为空，且不超过 {subjectMaxLength} 个字符；不得包含 type、scope、冒号或结尾句号。
            body 只能概述已提供信息明确支持的实现细节，否则设为 null。
            breakingChange 只能在已提供信息明确表明存在不兼容变更时填写，否则设为 null。
            issueNumbers 只能包含已提供信息中明确出现的数字 Issue 编号，否则必须为 []。
            JSON 字段必须且只能为 type、scope、subject、body、breakingChange、issueNumbers。
            scope、body、breakingChange 可为 null，issueNumbers 必须为整数数组。
            """;

    private boolean enabled;
    private String endpoint = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private String apiPath = "/chat/completions";
    private String model = "qwen3.7-max";
    private double temperature = 0.7D;
    private int maxTokens = 1024;
    private String systemPrompt = DEFAULT_SYSTEM_PROMPT;
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
        if (systemPrompt == null || systemPrompt.isBlank()) {
            systemPrompt = DEFAULT_SYSTEM_PROMPT;
        }
    }
}
