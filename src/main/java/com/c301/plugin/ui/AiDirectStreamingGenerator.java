package com.c301.plugin.ui;

import com.c301.plugin.application.ai.AiSuggestionValidator;
import com.c301.plugin.config.AiPreferencesState;
import com.c301.plugin.config.CommitTemplateSettingsResolver;
import com.c301.plugin.config.EffectiveCommitTemplateSettings;
import com.c301.plugin.domain.ai.AiCredentials;
import com.c301.plugin.domain.ai.AiGenerationError;
import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.domain.ai.AiStreamingListener;
import com.c301.plugin.infrastructure.ai.AiCommitTemplateContextRenderer;
import com.c301.plugin.infrastructure.ai.AiProviderFactory;
import com.c301.plugin.infrastructure.ai.AiSuggestionParser;
import com.c301.plugin.infrastructure.ai.AiSystemPromptTemplates;
import com.c301.plugin.infrastructure.credentials.PasswordSafeAiCredentialStore;
import com.c301.plugin.platform.vcs.AiIncludedChangesCollector;
import com.c301.plugin.utils.CommUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 未开启发送前检查时的直接生成流程。
 * 流式响应仅以已识别字段生成提交信息草稿，完整响应通过本地校验后才保留最终结果。
 */
public final class AiDirectStreamingGenerator {
    private final Project project;
    private final CommitMessageI commitMessage;
    private final AiIncludedChangesCollector.CollectionResult changes;
    private final AiPreferencesState preferences = AiPreferencesState.getInstance();
    private final String previousMessage;
    private final StringBuilder response = new StringBuilder();
    private final AtomicBoolean completed = new AtomicBoolean();

    private EffectiveCommitTemplateSettings effectiveSettings;
    private AiGenerationRequest request;

    public AiDirectStreamingGenerator(Project project, CommitMessageI commitMessage,
                                      AiIncludedChangesCollector.CollectionResult changes) {
        this.project = project;
        this.commitMessage = commitMessage;
        this.changes = changes;
        this.previousMessage = commitMessage instanceof CheckinProjectPanel panel ? panel.getCommitMessage() : "";
    }

    private static String text(String key) {
        return CommUtil.i18nResourceBundle(null).getString(key);
    }

    public void generate() {
        effectiveSettings = CommitTemplateSettingsResolver.getInstance(project).resolve();
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project,
                text("plugin.ai.generationTaskTitle"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                AiIncludedChangesCollector.DiffCollectionResult result = AiIncludedChangesCollector.collectDiff(project, changes);
                ApplicationManager.getApplication().invokeLater(() -> onDiffPrepared(result));
            }
        });
    }

    private void onDiffPrepared(AiIncludedChangesCollector.DiffCollectionResult result) {
        if (result.diff().isBlank()) {
            AiGenerationDialog.notifyNoEligibleChanges(project);
            return;
        }
        if (!AiDataTransferConsentDialog.ensureAccepted(project)) {
            return;
        }
        String apiKey = new PasswordSafeAiCredentialStore().readApiKey(preferences.getApiUrl());
        if (apiKey == null || apiKey.isBlank()) {
            notify(text("plugin.ai.apiKeyMissingHint"), NotificationType.ERROR);
            return;
        }
        String prompt = preferences.getCustomSystemPrompts().get(preferences.getProviderType());
        if (prompt == null || prompt.isBlank()) {
            prompt = AiSystemPromptTemplates.forProvider(preferences.getProviderType());
        }
        request = new AiGenerationRequest(preferences.getApiUrl(), preferences.getModel(), prompt,
                preferences.getTemperature(), preferences.getMaxTokens(), preferences.getQwenGenerationOptions(),
                effectiveSettings.language(), allowedTypes(), effectiveSettings.commitMessageRules(),
                AiCommitTemplateContextRenderer.render(effectiveSettings, allowedTypes()), result.diff());
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project,
                text("plugin.ai.generationTaskTitle"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                AiProviderFactory.create(preferences.getProviderType()).generate(request, new AiCredentials(apiKey), indicator,
                        new StreamingListener());
            }
        });
    }

    private List<com.c301.plugin.model.CommitTypeDomain> allowedTypes() {
        return effectiveSettings.customEnable() ? effectiveSettings.customCommitTypeList()
                : CommUtil.getDefaultCommitTypeList(effectiveSettings.language().getKey());
    }

    private void updateDraft() {
        String draft = IncrementalSuggestionDraft.render(response.toString());
        if (draft.isBlank()) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> commitMessage.setCommitMessage(draft));
    }

    private void complete() {
        if (!completed.compareAndSet(false, true)) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                var suggestion = AiSuggestionParser.parse(response.toString());
                var commit = AiSuggestionValidator.validateAndConvert(suggestion, effectiveSettings, allowedTypes());
                commitMessage.setCommitMessage(com.c301.plugin.domain.commit.CommitMessageFormatter.format(
                        commit, effectiveSettings.emojiEnable() ? effectiveSettings.emojiLocation() : null,
                        effectiveSettings.commitMessageRules()));
                notify(text("plugin.ai.directGenerationSuccess"), NotificationType.INFORMATION);
            } catch (Exception exception) {
                restorePreviousMessage(text("plugin.ai.directGenerationInvalid"));
            }
        });
    }

    private void fail(AiGenerationError error) {
        if (!completed.compareAndSet(false, true)) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> restorePreviousMessage(error.message()));
    }

    private void restorePreviousMessage(String message) {
        commitMessage.setCommitMessage(previousMessage);
        notify(message, NotificationType.ERROR);
    }

    private void notify(String message, NotificationType type) {
        PluginNotifications.notify(project, message, type);
    }

    private final class StreamingListener implements AiStreamingListener {
        @Override
        public void onText(String text) {
            response.append(text);
            updateDraft();
        }

        @Override
        public void onComplete() {
            complete();
        }

        @Override
        public void onError(AiGenerationError error) {
            fail(error);
        }
    }
}
