package com.c301.plugin.ui;

import com.c301.plugin.application.ai.AiSuggestionValidator;
import com.c301.plugin.config.AiGenerationConfigSnapshot;
import com.c301.plugin.config.AiPreferencesState;
import com.c301.plugin.config.CommitTemplateSettingsResolver;
import com.c301.plugin.config.EffectiveCommitTemplateSettings;
import com.c301.plugin.domain.ai.AiCredentials;
import com.c301.plugin.domain.ai.AiGenerationError;
import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.domain.ai.AiStreamingListener;
import com.c301.plugin.infrastructure.ai.AiProviderFactory;
import com.c301.plugin.infrastructure.ai.AiSuggestionParser;
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
    private final AtomicBoolean userTookOver = new AtomicBoolean();
    private final AtomicBoolean interruptionNotified = new AtomicBoolean();

    private volatile ProgressIndicator generationIndicator;
    private volatile String lastAiDraft;
    private EffectiveCommitTemplateSettings effectiveSettings;
    private AiGenerationConfigSnapshot generationConfig;
    private AiGenerationRequest request;

    public AiDirectStreamingGenerator(Project project, CommitMessageI commitMessage,
                                      AiIncludedChangesCollector.CollectionResult changes) {
        this.project = project;
        this.commitMessage = commitMessage;
        this.changes = changes;
        this.previousMessage = commitMessage instanceof CheckinProjectPanel panel ? panel.getCommitMessage() : "";
        this.lastAiDraft = previousMessage;
    }

    private static String text(String key) {
        return CommUtil.i18nResourceBundle(null).getString(key);
    }

    public void generate() {
        effectiveSettings = CommitTemplateSettingsResolver.getInstance(project).resolve();
        generationConfig = AiGenerationConfigSnapshot.capture(preferences, effectiveSettings);
        effectiveSettings = generationConfig.effectiveSettings();
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project,
                text("plugin.ai.generationTaskTitle"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                AiIncludedChangesCollector.DiffCollectionResult result =
                        AiIncludedChangesCollector.collectDiff(project, changes, indicator);
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (!project.isDisposed()) {
                        onDiffPrepared(result);
                    }
                });
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
        if (!canAiReplaceCommitMessage()) {
            return;
        }
        request = generationConfig.createRequest(result.diff());
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project,
                text("plugin.ai.generationTaskTitle"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                generationIndicator = indicator;
                if (userTookOver.get()) {
                    indicator.cancel();
                    return;
                }
                String apiKey = new PasswordSafeAiCredentialStore().readApiKey(generationConfig.providerType());
                if (apiKey == null || apiKey.isBlank()) {
                    ApplicationManager.getApplication().invokeLater(() -> AiDirectStreamingGenerator.this.notify(
                            text("plugin.ai.apiKeyMissingHint"), NotificationType.ERROR));
                    return;
                }
                AiProviderFactory.create(generationConfig.providerType()).generate(request, new AiCredentials(apiKey), indicator,
                        new StreamingListener());
            }
        });
    }

    private List<com.c301.plugin.model.CommitTypeDomain> allowedTypes() {
        return generationConfig.allowedTypes();
    }

    private void updateDraft() {
        String draft = IncrementalSuggestionDraft.render(response.toString());
        if (draft.isBlank()) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed() || completed.get() || !canAiReplaceCommitMessage()) {
                return;
            }
            lastAiDraft = draft;
            commitMessage.setCommitMessage(draft);
        });
    }

    private void complete() {
        if (!completed.compareAndSet(false, true)) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed() || !canAiReplaceCommitMessage()) {
                return;
            }
            try {
                var suggestion = AiSuggestionParser.parse(response.toString());
                var commit = AiSuggestionValidator.validateAndConvert(suggestion, effectiveSettings, allowedTypes());
                String formattedCommit = com.c301.plugin.domain.commit.CommitMessageFormatter.format(
                        commit, effectiveSettings.emojiEnable() ? effectiveSettings.emojiLocation() : null,
                        effectiveSettings.commitMessageRules());
                lastAiDraft = formattedCommit;
                commitMessage.setCommitMessage(formattedCommit);
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
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!project.isDisposed() && canAiReplaceCommitMessage()) {
                restorePreviousMessage(error.message());
            }
        });
    }

    private void restorePreviousMessage(String message) {
        lastAiDraft = previousMessage;
        commitMessage.setCommitMessage(previousMessage);
        notify(message, NotificationType.ERROR);
    }

    /**
     * 只允许覆盖仍由本次 AI 生成写入的文本；用户手动编辑后立即让出控制权。
     */
    private boolean canAiReplaceCommitMessage() {
        if (userTookOver.get()) {
            return false;
        }
        String currentMessage = currentCommitMessage();
        if (currentMessage == null || lastAiDraft == null || lastAiDraft.equals(currentMessage)) {
            return true;
        }
        if (userTookOver.compareAndSet(false, true)) {
            ProgressIndicator indicator = generationIndicator;
            if (indicator != null) {
                indicator.cancel();
            }
            if (interruptionNotified.compareAndSet(false, true)) {
                notify(text("plugin.ai.directGenerationStoppedByUser"), NotificationType.INFORMATION);
            }
        }
        return false;
    }

    private String currentCommitMessage() {
        return commitMessage instanceof CheckinProjectPanel panel ? panel.getCommitMessage() : null;
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
