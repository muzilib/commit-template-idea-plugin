package com.c301.plugin.ui;

import com.c301.plugin.application.ai.AiSuggestionValidator;
import com.c301.plugin.config.AiPreferencesState;
import com.c301.plugin.config.CommitTemplateSettingsResolver;
import com.c301.plugin.config.EffectiveCommitTemplateSettings;
import com.c301.plugin.config.UnifiedCommitTemplateSettingsConfigurable;
import com.c301.plugin.domain.ai.AiCredentials;
import com.c301.plugin.domain.ai.AiGenerationError;
import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.domain.ai.AiStreamingListener;
import com.c301.plugin.infrastructure.ai.*;
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
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI 提交建议窗口：发送前完整展示实际请求参数，响应只会回填 Commit Message。
 */
public final class AiGenerationDialog extends JDialog {
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
    private final AtomicBoolean completed = new AtomicBoolean();

    private EffectiveCommitTemplateSettings effectiveSettings;
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
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project,
                text("plugin.ai.generationTaskTitle"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                AiIncludedChangesCollector.DiffCollectionResult result = AiIncludedChangesCollector.collectDiff(project, changes);
                ApplicationManager.getApplication().invokeLater(() -> onContextPrepared(result));
            }
        });
    }

    private void onContextPrepared(AiIncludedChangesCollector.DiffCollectionResult result) {
        if (!isDisplayable()) {
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
        String prompt = preferences.getCustomSystemPrompts().get(preferences.getProviderType());
        if (prompt == null || prompt.isBlank()) {
            prompt = AiSystemPromptTemplates.forProvider(preferences.getProviderType());
        }
        request = new AiGenerationRequest(preferences.getApiUrl(), preferences.getModel(), prompt,
                preferences.getTemperature(), preferences.getMaxTokens(), preferences.getQwenGenerationOptions(),
                preferences.getDeepSeekGenerationOptions(), preferences.getOpenAiGenerationOptions(),
                                effectiveSettings.language(), allowedTypes(),
                effectiveSettings.commitMessageRules(), AiCommitTemplateContextRenderer.render(effectiveSettings, allowedTypes()),
                result.diff());
        try {
            requestPreview = renderRequestPreview(result);
        } catch (Exception exception) {
            preview.setText(text("plugin.ai.requestPreviewError"));
            return;
        }
        preview.setText(requestPreview);
        if (preferences.isCheckDiffBeforeSending()) {
            reviewHint.setVisible(true);
            generate.setEnabled(true);
            return;
        }
        preview.append("\n\n" + text("plugin.ai.sendingWithoutCheck"));
        generate();
    }

    private String renderRequestPreview(AiIncludedChangesCollector.DiffCollectionResult result) throws Exception {
        StringBuilder value = new StringBuilder();
        var provider = AiProviderFactory.create(preferences.getProviderType());
        boolean openAiResponses = preferences.getProviderType() == com.c301.plugin.domain.ai.AiProviderType.CHATGPT;
        value.append("POST ").append(openAiResponses ? OpenAiResponsesRequestRenderer.resolveUrl(request)
                        : OpenAiCompatibleRequestRenderer.resolveUrl(request))
                .append("\n\nRequest headers\n")
                .append("Accept: text/event-stream\n")
                .append("Content-Type: application/json\n")
                .append("Authorization: Bearer [configured; hidden]\n");
        if (preferences.getProviderType() == com.c301.plugin.domain.ai.AiProviderType.QWEN
                && preferences.getQwenGenerationOptions().isDataInspectionEnabled()) {
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
        response.setLength(0);
        completed.set(false);
        preview.setText(requestPreview + "\n\n----------------------------------------\nAI response\n");
        apply.setEnabled(false);
        generate.setEnabled(false);
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project,
                text("plugin.ai.generationTaskTitle"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String apiKey = new PasswordSafeAiCredentialStore().readApiKey(preferences.getProviderType());
                if (apiKey == null || apiKey.isBlank()) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        String message = text("plugin.ai.apiKeyMissingHint");
                        Messages.showWarningDialog(AiGenerationDialog.this, message, text("plugin.ai.apiKeyMissingTitle"));
                        AiGenerationDialog.notify(project, message, NotificationType.ERROR);
                    });
                    return;
                }
                AiProviderFactory.create(preferences.getProviderType()).generate(request, new AiCredentials(apiKey), indicator,
                        new Listener());
            }
        });
    }

    private void applySuggestion() {
        try {
            var suggestion = AiSuggestionParser.parse(response.toString());
            var commit = AiSuggestionValidator.validateAndConvert(suggestion, effectiveSettings, allowedTypes());
            commitMessage.setCommitMessage(com.c301.plugin.domain.commit.CommitMessageFormatter.format(
                    commit, effectiveSettings.emojiEnable() ? effectiveSettings.emojiLocation() : null,
                    effectiveSettings.commitMessageRules()));
            dispose();
        } catch (Exception exception) {
            Messages.showErrorDialog(this, exception.getMessage(), text("plugin.ai.applyErrorTitle"));
        }
    }

    private void openSettings() {
        dispose();
        UnifiedCommitTemplateSettingsConfigurable.requestAiModelTabOnOpen();
        com.intellij.openapi.options.ShowSettingsUtil.getInstance().showSettingsDialog(project,
                "plugins.muzilib.commit.template");
    }

    private java.util.List<com.c301.plugin.model.CommitTypeDomain> allowedTypes() {
        return effectiveSettings.customEnable() ? effectiveSettings.customCommitTypeList()
                : CommUtil.getDefaultCommitTypeList(effectiveSettings.language().getKey());
    }

    private final class Listener implements AiStreamingListener {
        @Override
        public void onText(String text) {
            response.append(text);
            ApplicationManager.getApplication().invokeLater(() -> preview.append(text));
        }

        @Override
        public void onComplete() {
            if (!completed.compareAndSet(false, true)) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(() -> {
                generate.setEnabled(true);
                if (response.isEmpty()) {
                    preview.append("\n\n" + text("plugin.ai.emptyVisibleResponse"));
                    AiGenerationDialog.notify(project, text("plugin.ai.emptyVisibleResponse"), NotificationType.ERROR);
                    return;
                }
                try {
                    AiSuggestionValidator.validateAndConvert(AiSuggestionParser.parse(response.toString()), effectiveSettings, allowedTypes());
                    apply.setEnabled(true);
                    preview.append("\n\n" + text("plugin.ai.completed")
                            .replace("{target}", text("plugin.ai.target.commitMessage")));
                } catch (Exception exception) {
                    preview.append("\n\n" + text("plugin.ai.invalidSuggestion"));
                }
            });
        }

        @Override
        public void onError(AiGenerationError error) {
            if (!completed.compareAndSet(false, true)) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(() -> {
                generate.setEnabled(true);
                preview.append("\n\n-- " + error.message() + " --");
                notifyAiError(project, error);
            });
        }
    }
}
