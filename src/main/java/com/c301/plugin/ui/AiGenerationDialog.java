package com.c301.plugin.ui;

import com.c301.plugin.application.ai.AiSuggestionValidator;
import com.c301.plugin.config.*;
import com.c301.plugin.domain.ai.AiCredentials;
import com.c301.plugin.domain.ai.AiGenerationError;
import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.domain.ai.AiStreamingListener;
import com.c301.plugin.infrastructure.ai.AiProviderFactory;
import com.c301.plugin.infrastructure.ai.AiSuggestionParser;
import com.c301.plugin.infrastructure.ai.OpenAiCompatibleRequestRenderer;
import com.c301.plugin.infrastructure.ai.OpenAiResponsesRequestRenderer;
import com.c301.plugin.infrastructure.credentials.PasswordSafeAiCredentialStore;
import com.c301.plugin.platform.vcs.AiIncludedChangesCollector;
import com.c301.plugin.utils.CommUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 提交建议窗口：发送前完整展示实际请求参数，响应只会回填 Commit Message。
 */
public final class AiGenerationDialog extends JDialog {
    private static final long STREAM_UPDATE_INTERVAL_MILLIS = 100L;

    private final Project project;
    private final CommitMessageI commitMessage;
    private final AiPreferencesState preferences = AiPreferencesState.getInstance();
    private final AiIncludedChangesCollector.CollectionResult changes;
    private final JPanel reviewHint = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final JTextArea preview = new JTextArea();
    private final JButton generate = new JButton();
    private final JButton apply = new JButton();
    private final JButton settings = new JButton();
    private final JButton close = new JButton();
    private final StringBuilder response = new StringBuilder();
    private final Object responseLock = new Object();
    private final StringBuilder pendingPreviewText = new StringBuilder();
    private final Object pendingPreviewLock = new Object();
    private final AtomicBoolean completed = new AtomicBoolean();
    private final AtomicBoolean previewUpdateScheduled = new AtomicBoolean();
    private final AtomicLong generationSequence = new AtomicLong();
    private volatile boolean disposed;
    private volatile long activeGenerationId;
    private volatile ProgressIndicator activeIndicator;

    private EffectiveCommitTemplateSettings effectiveSettings;
    private AiGenerationConfigSnapshot generationConfig;
    private AiGenerationRequest request;
    private String requestPreview;

    public AiGenerationDialog(Project project, CommitMessageI commitMessage,
                              AiIncludedChangesCollector.CollectionResult changes) {
        this.project = project;
        this.commitMessage = commitMessage;
        this.changes = changes;
        setTitle(text("plugin.ai.generationDialogTitle"));
        setModal(true);
        setMinimumSize(new Dimension(620, 480));
        setPreferredSize(new Dimension(900, 700));
        generate.setText(text("plugin.ai.confirmAndGenerate"));
        apply.setText(text("plugin.ai.applyToCommitMessage"));
        settings.setText("<html><a href='settings'>" + text("plugin.ai.openSettings") + "</a></html>");
        settings.setBorderPainted(false);
        settings.setContentAreaFilled(false);
        settings.setFocusPainted(false);
        settings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.setText(text("plugin.ai.close"));
        setContentPane(createContent());
        pack();
        setLocationRelativeTo(null);
        generate.addActionListener(event -> generate());
        apply.addActionListener(event -> applySuggestion());
        settings.addActionListener(event -> openSettings());
        close.addActionListener(event -> dispose());
        getRootPane().registerKeyboardAction(event -> dispose(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        prepareContext();
    }

    public static void notifyNoEligibleChanges(Project project) {
        notify(project, text("plugin.ai.noEligibleChangesNotification"), NotificationType.WARNING);
    }

    private static void notifyAiError(Project project, AiGenerationError error) {
        NotificationType type = error.kind() == AiGenerationError.Kind.CANCELED
                ? NotificationType.INFORMATION : NotificationType.ERROR;
        notify(project, error.message(), type);
    }

    private static void notify(Project project, String message, NotificationType type) {
        PluginNotifications.notify(project, message, type);
    }

    private static void appendLines(StringBuilder value, java.util.List<String> entries) {
        for (String entry : entries) {
            value.append("- ").append(entry).append("\n");
        }
    }

    private static String text(String key) {
        return CommUtil.i18nResourceBundle(null).getString(key);
    }

    private JComponent createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(JBUI.Borders.empty(12));
        JLabel hintLabel = new JLabel(text("plugin.ai.checkBeforeSendingHint"));
        reviewHint.add(hintLabel);
        reviewHint.add(Box.createHorizontalStrut(JBUI.scale(6)));
        reviewHint.add(settings);
        reviewHint.setVisible(false);
        content.add(reviewHint, BorderLayout.NORTH);

        configureTextArea(preview);
        preview.setText(text("plugin.ai.diffPreparing"));
        JScrollPane previewScroll = new JScrollPane(preview);
        previewScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        content.add(previewScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        generate.setEnabled(false);
        apply.setEnabled(false);
        actions.add(generate);
        actions.add(apply);
        actions.add(close);
        content.add(actions, BorderLayout.SOUTH);
        return content;
    }

    private void configureTextArea(JTextArea textArea) {
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, textArea.getFont().getSize()));
        textArea.setLineWrap(false);
    }

    private void prepareContext() {
        if (changes.includedMetadata().isEmpty()) {
            notifyNoEligibleChanges(project);
            dispose();
            return;
        }
        effectiveSettings = CommitTemplateSettingsResolver.getInstance(project).resolve();
        generationConfig = AiGenerationConfigSnapshot.capture(preferences, effectiveSettings);
        effectiveSettings = generationConfig.effectiveSettings();
        long contextGenerationId = generationSequence.incrementAndGet();
        activeGenerationId = contextGenerationId;
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project,
                text("plugin.ai.generationTaskTitle"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                activeIndicator = indicator;
                AiIncludedChangesCollector.DiffCollectionResult result =
                        AiIncludedChangesCollector.collectDiff(project, changes, indicator);
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (AiGenerationDialog.this.isGenerationActive(contextGenerationId)) {
                        onContextPrepared(result, contextGenerationId);
                    }
                });
            }
        });
    }

    private void onContextPrepared(AiIncludedChangesCollector.DiffCollectionResult result, long contextGenerationId) {
        if (!isGenerationActive(contextGenerationId)) {
            return;
        }
        if (result.diff().isBlank()) {
            notifyNoEligibleChanges(project);
            dispose();
            return;
        }
        if (!AiDataTransferConsentDialog.ensureAccepted(project)) {
            dispose();
            return;
        }
        request = generationConfig.createRequest(result.diff());
        try {
            requestPreview = renderRequestPreview(result);
        } catch (Exception exception) {
            preview.setText(text("plugin.ai.requestPreviewError"));
            return;
        }
        preview.setText(requestPreview);
        if (generationConfig.checkDiffBeforeSending()) {
            reviewHint.setVisible(true);
            generate.setEnabled(true);
            return;
        }
        preview.append("\n\n" + text("plugin.ai.sendingWithoutCheck"));
        generate();
    }

    private String renderRequestPreview(AiIncludedChangesCollector.DiffCollectionResult result) throws Exception {
        StringBuilder value = new StringBuilder();
        var provider = AiProviderFactory.create(generationConfig.providerType());
        boolean openAiResponses = generationConfig.providerType() == com.c301.plugin.domain.ai.AiProviderType.CHATGPT;
        value.append("POST ").append(openAiResponses ? OpenAiResponsesRequestRenderer.resolveUrl(request)
                        : OpenAiCompatibleRequestRenderer.resolveUrl(request))
                .append("\n\nRequest headers\n")
                .append("Accept: text/event-stream\n")
                .append("Content-Type: application/json\n")
                .append("Authorization: Bearer [configured; hidden]\n");
        if (generationConfig.isQwenDataInspectionEnabled()) {
            value.append("X-DashScope-DataInspection: {\"input\":\"cip\",\"output\":\"cip\"}\n");
        }
        value.append("\nRequest body\n")
                .append(openAiResponses ? OpenAiResponsesRequestRenderer.formattedRequestBody(request)
                        : OpenAiCompatibleRequestRenderer.formattedRequestBody(request, provider))
                .append("\n\nLocal filtering result\n")
                .append("Included files: ").append(result.includedFileCount()).append("\n")
                .append("Diff characters: ").append(result.characterCount()).append("\n")
                .append("Diff truncated: ").append(result.truncated() ? "yes" : "no");
        if (!changes.excludedChanges().isEmpty() || !result.excludedChanges().isEmpty()) {
            value.append("\nExcluded changes:\n");
            appendLines(value, changes.excludedChanges());
            appendLines(value, result.excludedChanges());
        }
        return value.toString();
    }

    private void generate() {
        if (request == null) {
            return;
        }
        synchronized (responseLock) {
            response.setLength(0);
        }
        synchronized (pendingPreviewLock) {
            pendingPreviewText.setLength(0);
        }
        long requestGenerationId = generationSequence.incrementAndGet();
        activeGenerationId = requestGenerationId;
        completed.set(false);
        preview.setText(requestPreview + "\n\n----------------------------------------\nAI response\n");
        apply.setEnabled(false);
        generate.setEnabled(false);
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project,
                text("plugin.ai.generationTaskTitle"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                activeIndicator = indicator;
                String apiKey = new PasswordSafeAiCredentialStore().readApiKey(generationConfig.providerType());
                if (apiKey == null || apiKey.isBlank()) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!isGenerationActive(requestGenerationId)) {
                            return;
                        }
                        String message = text("plugin.ai.apiKeyMissingHint");
                        Messages.showWarningDialog(AiGenerationDialog.this, message, text("plugin.ai.apiKeyMissingTitle"));
                        PluginNotifications.notifyOpenAiModelSettings(project, message, NotificationType.ERROR);
                    });
                    return;
                }
                AiProviderFactory.create(generationConfig.providerType()).generate(request, new AiCredentials(apiKey), indicator,
                        new Listener(requestGenerationId));
            }
        });
    }

    private void applySuggestion() {
        try {
            var suggestion = AiSuggestionParser.parse(responseSnapshot());
            var commit = AiSuggestionValidator.validateAndConvert(suggestion, effectiveSettings, allowedTypes());
            AiCommitMessageSelectionSupport.setCommitMessage(commitMessage,
                    com.c301.plugin.domain.commit.CommitMessageFormatter.format(
                            commit, effectiveSettings.emojiEnable() ? effectiveSettings.emojiLocation() : null,
                            effectiveSettings.commitMessageRules()));
            dispose();
        } catch (Exception exception) {
            Messages.showErrorDialog(this, exception.getMessage(), text("plugin.ai.applyErrorTitle"));
        }
    }

    private void openSettings() {
        dispose();
        UnifiedCommitTemplateSettingsConfigurable.openAiModelSettings(project);
    }

    private boolean isUiActive() {
        return !disposed && isDisplayable() && !project.isDisposed();
    }

    private boolean isGenerationActive(long generationId) {
        return isUiActive() && activeGenerationId == generationId;
    }

    @Override
    public void dispose() {
        disposed = true;
        activeGenerationId = generationSequence.incrementAndGet();
        ProgressIndicator indicator = activeIndicator;
        if (indicator != null) {
            indicator.cancel();
        }
        super.dispose();
    }

    private java.util.List<com.c301.plugin.model.CommitTypeDomain> allowedTypes() {
        return generationConfig.allowedTypes();
    }

    private void appendResponse(String text) {
        synchronized (responseLock) {
            response.append(text);
        }
        synchronized (pendingPreviewLock) {
            pendingPreviewText.append(text);
        }
    }

    private String responseSnapshot() {
        synchronized (responseLock) {
            return response.toString();
        }
    }

    private String drainPendingPreviewText() {
        synchronized (pendingPreviewLock) {
            String pending = pendingPreviewText.toString();
            pendingPreviewText.setLength(0);
            return pending;
        }
    }

    private void schedulePreviewUpdate(long generationId) {
        if (!previewUpdateScheduled.compareAndSet(false, true)) {
            return;
        }
        AppExecutorUtil.getAppScheduledExecutorService().schedule(() ->
                ApplicationManager.getApplication().invokeLater(() -> {
                    previewUpdateScheduled.set(false);
                    if (isGenerationActive(generationId)) {
                        appendPendingPreviewText();
                    }
                }), STREAM_UPDATE_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void appendPendingPreviewText() {
        String pending = drainPendingPreviewText();
        if (isUiActive() && !pending.isEmpty()) {
            preview.append(pending);
        }
    }

    private final class Listener implements AiStreamingListener {
        private final long generationId;

        private Listener(long generationId) {
            this.generationId = generationId;
        }

        @Override
        public void onText(String text) {
            if (!isGenerationActive(generationId)) {
                return;
            }
            appendResponse(text);
            schedulePreviewUpdate(generationId);
        }

        @Override
        public void onComplete() {
            if (!isGenerationActive(generationId) || !completed.compareAndSet(false, true)) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(() -> {
                if (!isGenerationActive(generationId)) {
                    return;
                }
                appendPendingPreviewText();
                generate.setEnabled(true);
                String responseText = responseSnapshot();
                if (responseText.isEmpty()) {
                    preview.append("\n\n" + text("plugin.ai.emptyVisibleResponse"));
                    AiGenerationDialog.notify(project, text("plugin.ai.emptyVisibleResponse"), NotificationType.ERROR);
                    return;
                }
                try {
                    AiSuggestionValidator.validateAndConvert(AiSuggestionParser.parse(responseText), effectiveSettings, allowedTypes());
                    apply.setEnabled(true);
                    preview.append("\n\n" + text("plugin.ai.completed")
                            .replace("{target}", text("plugin.ai.target.commitMessage")));
                } catch (Exception exception) {
                    preview.append("\n\n" + text("plugin.ai.invalidSuggestion"));
                    if (exception instanceof AiSuggestionValidator.SubjectLengthLimitExceededException) {
                        PluginNotifications.notifyOpenCommitRules(project, exception.getMessage(), NotificationType.WARNING);
                    }
                }
            });
        }

        @Override
        public void onError(AiGenerationError error) {
            if (!isGenerationActive(generationId) || !completed.compareAndSet(false, true)) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(() -> {
                if (!isGenerationActive(generationId)) {
                    return;
                }
                appendPendingPreviewText();
                generate.setEnabled(true);
                preview.append("\n\n-- " + error.message() + " --");
                notifyAiError(project, error);
            });
        }
    }
}
