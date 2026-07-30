package com.c301.plugin.ui;

import com.c301.plugin.application.ai.AiSuggestionValidator;
import com.c301.plugin.config.AiPreferencesState;
import com.c301.plugin.config.CommitTemplateSettingsResolver;
import com.c301.plugin.config.EffectiveCommitTemplateSettings;
import com.c301.plugin.domain.ai.*;
import com.c301.plugin.infrastructure.ai.AiPromptRenderer;
import com.c301.plugin.infrastructure.ai.AiSuggestionParser;
import com.c301.plugin.infrastructure.ai.OpenAiCompatibleProvider;
import com.c301.plugin.infrastructure.credentials.PasswordSafeAiCredentialStore;
import com.c301.plugin.model.GitCommitDomain;
import com.c301.plugin.platform.vcs.AiIncludedChangesCollector;
import com.c301.plugin.utils.CommUtil;
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
import java.util.function.Consumer;

/**
 * AI 快速生成弹窗：显示发送摘要、流式原文和最终候选预览；仅用户确认后回填 Commit Message。
 */
public final class AiGenerationDialog extends JDialog {
    private static final boolean DEVELOPMENT_DIFF_PREVIEW = ApplicationManager.getApplication().isInternal()
            || Boolean.getBoolean("commit.template.ai.diff.preview");
    private final Project project;
    private final Consumer<GitCommitDomain> suggestionConsumer;
    private final AiPreferencesState preferences;
    private final AiIncludedChangesCollector.CollectionResult changes;
    private final JTextArea output = new JTextArea();
    private final JButton generate = new JButton();
    private final JButton apply = new JButton();
    private final JButton close = new JButton();
    private final JCheckBox sendDiff = new JCheckBox();
    private final StringBuilder response = new StringBuilder();
    private final AtomicBoolean completed = new AtomicBoolean();
    private AiIncludedChangesCollector.DiffCollectionResult preparedDiff;
    private String applicationTarget = "表单";


    public AiGenerationDialog(Project project, CommitMessageI commitMessage,
                              AiIncludedChangesCollector.CollectionResult changes) {
        this(project, changes, commit -> {
            EffectiveCommitTemplateSettings settings = CommitTemplateSettingsResolver.getInstance(project).resolve();
            commitMessage.setCommitMessage(com.c301.plugin.domain.commit.CommitMessageFormatter.format(
                    commit, settings.emojiEnable() ? settings.emojiLocation() : null, settings.commitMessageRules()));
        });
        applicationTarget = text("plugin.ai.target.commitMessage");
        apply.setText(text("plugin.ai.applyToCommitMessage"));
    }

    public AiGenerationDialog(Project project, AiIncludedChangesCollector.CollectionResult changes,
                              Consumer<GitCommitDomain> suggestionConsumer) {
        this.project = project;
        this.suggestionConsumer = suggestionConsumer;
        this.preferences = AiPreferencesState.getInstance();
        this.changes = changes;
        setTitle(text("plugin.ai.generationDialogTitle"));
        setModal(true);
        setMinimumSize(new Dimension(620, 480));
        setPreferredSize(new Dimension(760, 620));
        generate.setText(text("plugin.ai.generate"));
        apply.setText(text("plugin.ai.applyToForm"));
        close.setText(text("plugin.ai.close"));
        sendDiff.setText(text("plugin.ai.sendDiff"));
        setContentPane(createContent());
        pack();
        setLocationRelativeTo(null);
        generate.addActionListener(event -> generate());
        sendDiff.addActionListener(event -> onDiffSelectionChanged());
        apply.addActionListener(event -> applySuggestion());
        close.addActionListener(event -> dispose());
    }

    private static String text(String key) {
        return CommUtil.i18nResourceBundle(null).getString(key);
    }

    private JComponent createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(JBUI.Borders.empty(12));
        JTextArea summary = new JTextArea(createSummary());
        summary.setEditable(false);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        JScrollPane summaryScroll = new JScrollPane(summary);
        summaryScroll.setPreferredSize(new Dimension(0, 180));
        summaryScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(summaryScroll, BorderLayout.NORTH);

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        JScrollPane outputScroll = new JScrollPane(output);
        outputScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(outputScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sendDiff.setEnabled(preferences.isAllowDiffTransfer());
        sendDiff.setVisible(preferences.isAllowDiffTransfer());
        sendDiff.setToolTipText(text("plugin.ai.sendDiffTooltip"));
        apply.setEnabled(false);
        actions.add(sendDiff);
        actions.add(generate);
        actions.add(apply);
        actions.add(close);
        content.add(actions, BorderLayout.SOUTH);
        return content;
    }

    private String createSummary() {
        String summary = text("plugin.ai.summary.service") + " " + preferences.getEndpoint()
                + "\n" + text("plugin.ai.summary.model") + " " + preferences.getModel()
                + "\n" + text("plugin.ai.summary.included") + " " + changes.includedMetadata().size()
                + "\n" + text("plugin.ai.summary.excluded") + " " + changes.excludedChanges().size()
                + "\n\n" + text("plugin.ai.summary.metadata") + "\n" + changes.asPromptContent();
        if (DEVELOPMENT_DIFF_PREVIEW) {
            EffectiveCommitTemplateSettings settings = CommitTemplateSettingsResolver.getInstance(project).resolve();
            AiGenerationRequest request = createRequest(settings, AiTransferMode.METADATA, changes.asPromptContent());
            summary += "\n\n" + text("plugin.ai.debug.systemPrompt") + "\n"
                    + AiPromptRenderer.systemPrompt(request)
                    + "\n\n" + text("plugin.ai.debug.metadataUserPrompt") + "\n"
                    + AiPromptRenderer.userPrompt(request);
        }
        return summary + (changes.excludedChanges().isEmpty() ? "" : "\n\n" + text("plugin.ai.summary.excludedList")
                + "\n" + String.join("\n", changes.excludedChanges()));
    }

    private AiGenerationRequest createRequest(EffectiveCommitTemplateSettings settings, AiTransferMode transferMode,
                                              String changeContent) {
        return new AiGenerationRequest(preferences.getEndpoint(), preferences.getApiPath(), preferences.getModel(),
                preferences.getTemperature(), preferences.getMaxTokens(), preferences.getSystemPrompt(), settings.language(),
                allowedTypes(settings), settings.commitMessageRules(), transferMode, changeContent);
    }

    private void generate() {
        if (changes.includedMetadata().isEmpty()) {
            output.setText(text("plugin.ai.noIncludedChanges"));
            return;
        }
        String apiKey = new PasswordSafeAiCredentialStore().readApiKey(preferences.getEndpoint());
        if (apiKey == null || apiKey.isBlank()) {
            Messages.showWarningDialog(this, text("plugin.ai.apiKeyMissingHint"), text("plugin.ai.apiKeyMissingTitle"));
            return;
        }
        EffectiveCommitTemplateSettings settings = CommitTemplateSettingsResolver.getInstance(project).resolve();
        AiTransferMode transferMode = sendDiff.isSelected() ? AiTransferMode.DIFF : AiTransferMode.METADATA;
        if (transferMode == AiTransferMode.DIFF && DEVELOPMENT_DIFF_PREVIEW && preparedDiff == null) {
            prepareDiffPreview();
            return;
        }
        String changeContent = transferMode == AiTransferMode.DIFF
                ? (preparedDiff != null ? preparedDiff.diff()
                : AiIncludedChangesCollector.collectDiff(project, changes).diff())
                : changes.asPromptContent();
        if (changeContent.isBlank()) {
            output.setText(transferMode == AiTransferMode.DIFF
                    ? text("plugin.ai.noDiff")
                    : text("plugin.ai.noMetadata"));
            return;
        }
        response.setLength(0);
        completed.set(false);
        output.setText("");
        apply.setEnabled(false);
        generate.setEnabled(false);
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project,
                text("plugin.ai.generationTaskTitle"), true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {

                AiGenerationRequest request = createRequest(settings, transferMode, changeContent);
                new OpenAiCompatibleProvider().generate(request, new AiCredentials(apiKey), progressIndicator,
                        new Listener(settings));
            }
        });
    }

    /**
     * 开发环境中，先在内存中构建并审阅实际发送的 Diff，避免将未展示的内容直接发往远程服务。
     */
    private void prepareDiffPreview() {
        generate.setEnabled(false);
        output.setText(text("plugin.ai.diffPreviewPreparing"));
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project,
                text("plugin.ai.generationTaskTitle"), true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                AiIncludedChangesCollector.DiffCollectionResult result = AiIncludedChangesCollector.collectDiff(project, changes);
                ApplicationManager.getApplication().invokeLater(() -> {
                    preparedDiff = result;
                    generate.setEnabled(true);
                    generate.setText(text("plugin.ai.confirmAndGenerate"));
                    output.setText(renderDiffPreview(result));
                });
            }
        });
    }

    private String renderDiffPreview(AiIncludedChangesCollector.DiffCollectionResult result) {
        String preview = text("plugin.ai.diffPreviewReady")
                .replace("{files}", String.valueOf(result.includedFileCount()))
                .replace("{characters}", String.valueOf(result.characterCount()));
        if (result.truncated()) {
            preview += "\n" + text("plugin.ai.diffPreviewTruncated");
        }
        if (!result.excludedChanges().isEmpty()) {
            preview += "\n\n" + text("plugin.ai.summary.excludedList") + "\n"
                    + String.join("\n", result.excludedChanges());
        }
        return preview + "\n\n" + result.diff();
    }

    private void onDiffSelectionChanged() {
        preparedDiff = null;
        generate.setText(text("plugin.ai.generate"));
        if (DEVELOPMENT_DIFF_PREVIEW && sendDiff.isSelected()) {
            prepareDiffPreview();
        } else if (DEVELOPMENT_DIFF_PREVIEW) {
            output.setText("");
        }
    }

    private void applySuggestion() {
        try {
            EffectiveCommitTemplateSettings settings = CommitTemplateSettingsResolver.getInstance(project).resolve();
            var suggestion = AiSuggestionParser.parse(response.toString());
            var commit = AiSuggestionValidator.validateAndConvert(suggestion, settings, allowedTypes(settings));
            suggestionConsumer.accept(commit);
            dispose();
        } catch (Exception exception) {
            Messages.showErrorDialog(this, exception.getMessage(), text("plugin.ai.applyErrorTitle"));
        }
    }

    private java.util.List<com.c301.plugin.model.CommitTypeDomain> allowedTypes(EffectiveCommitTemplateSettings settings) {
        return settings.customEnable() ? settings.customCommitTypeList()
                : CommUtil.getDefaultCommitTypeList(settings.language().getKey());
    }

    private final class Listener implements AiStreamingListener {
        private final EffectiveCommitTemplateSettings settings;

        private Listener(EffectiveCommitTemplateSettings settings) {
            this.settings = settings;
        }

        @Override
        public void onText(String text) {
            response.append(text);
            ApplicationManager.getApplication().invokeLater(() -> output.append(text));
        }

        @Override
        public void onComplete() {
            if (!completed.compareAndSet(false, true)) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(() -> {
                generate.setEnabled(true);
                try {
                    AiSuggestionValidator.validateAndConvert(AiSuggestionParser.parse(response.toString()), settings, allowedTypes(settings));
                    apply.setEnabled(true);
                    output.append("\n\n" + text("plugin.ai.completed").replace("{target}", applicationTarget));
                } catch (Exception exception) {
                    output.append("\n\n" + text("plugin.ai.invalidSuggestion"));
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
                output.append("\n\n—— " + error.message() + " ——");
            });
        }
    }
}
