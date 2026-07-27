package com.c301.plugin.ui;

import com.c301.plugin.application.ai.AiSuggestionValidator;
import com.c301.plugin.config.AiPreferencesState;
import com.c301.plugin.config.CommitTemplateSettingsResolver;
import com.c301.plugin.config.EffectiveCommitTemplateSettings;
import com.c301.plugin.domain.ai.*;
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
    private final Project project;
    private final Consumer<GitCommitDomain> suggestionConsumer;
    private final AiPreferencesState preferences;
    private final AiIncludedChangesCollector.CollectionResult changes;
    private final JTextArea output = new JTextArea();
    private final JButton generate = new JButton("开始生成");
    private final JButton apply = new JButton("应用到表单");
    private final JButton close = new JButton("关闭");
    private final JCheckBox sendDiff = new JCheckBox("本次发送经过筛选的 Diff");
    private final StringBuilder response = new StringBuilder();
    private final AtomicBoolean completed = new AtomicBoolean();
    private String applicationTarget = "表单";


    public AiGenerationDialog(Project project, CommitMessageI commitMessage,
                              AiIncludedChangesCollector.CollectionResult changes) {
        this(project, changes, commit -> {
            EffectiveCommitTemplateSettings settings = CommitTemplateSettingsResolver.getInstance(project).resolve();
            commitMessage.setCommitMessage(com.c301.plugin.domain.commit.CommitMessageFormatter.format(
                    commit, settings.emojiEnable() ? settings.emojiLocation() : null, settings.commitMessageRules()));
        });
        applicationTarget = "提交信息";
        apply.setText("应用到提交信息");
    }

    public AiGenerationDialog(Project project, AiIncludedChangesCollector.CollectionResult changes,
                              Consumer<GitCommitDomain> suggestionConsumer) {
        this.project = project;
        this.suggestionConsumer = suggestionConsumer;
        this.preferences = AiPreferencesState.getInstance();
        this.changes = changes;
        setTitle("AI 生成提交信息");
        setModal(true);
        setMinimumSize(new Dimension(620, 480));
        setPreferredSize(new Dimension(760, 620));
        setContentPane(createContent());
        pack();
        setLocationRelativeTo(null);
        generate.addActionListener(event -> generate());
        apply.addActionListener(event -> applySuggestion());
        close.addActionListener(event -> dispose());
    }

    private JComponent createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(JBUI.Borders.empty(12));
        JTextArea summary = new JTextArea("服务：" + preferences.getEndpoint() + "\n模型：" + preferences.getModel()
                + "\n包含文件：" + changes.includedMetadata().size() + "\n排除文件：" + changes.excludedChanges().size()
                + "\n\n将发送的元数据：\n" + changes.asPromptContent()
                + (changes.excludedChanges().isEmpty() ? "" : "\n\n已排除：\n" + String.join("\n", changes.excludedChanges())));
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
        sendDiff.setToolTipText("仅在本次确认后构建并发送经过筛选和总量限制的 Diff。");
        apply.setEnabled(false);
        actions.add(sendDiff);
        actions.add(generate);
        actions.add(apply);
        actions.add(close);
        content.add(actions, BorderLayout.SOUTH);
        return content;
    }

    private void generate() {
        if (changes.includedMetadata().isEmpty()) {
            output.setText("没有可安全发送的已包含变更。请检查文件选择或排除规则。");
            return;
        }
        String apiKey = new PasswordSafeAiCredentialStore().readApiKey(preferences.getEndpoint());
        if (apiKey == null || apiKey.isBlank()) {
            Messages.showWarningDialog(this, "请先在偏好设置中配置当前 AI 服务地址对应的 API Key。", "未配置 API Key");
            return;
        }
        EffectiveCommitTemplateSettings settings = CommitTemplateSettingsResolver.getInstance(project).resolve();
        AiTransferMode transferMode = sendDiff.isSelected() ? AiTransferMode.DIFF : AiTransferMode.METADATA;
        String changeContent = transferMode == AiTransferMode.DIFF
                ? AiIncludedChangesCollector.collectDiff(project, changes)
                : changes.asPromptContent();
        if (changeContent.isBlank()) {
            output.setText(transferMode == AiTransferMode.DIFF
                    ? "没有可安全发送的 Diff；不会发起网络请求。"
                    : "没有可安全发送的变更元数据。不会发起网络请求。");
            return;
        }
        response.setLength(0);
        completed.set(false);
        output.setText("");
        apply.setEnabled(false);
        generate.setEnabled(false);
        com.intellij.openapi.progress.ProgressManager.getInstance().run(new Task.Backgroundable(project, "AI 生成提交信息", true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {

                var allowedTypes = settings.customEnable() ? settings.customCommitTypeList()
                        : CommUtil.getDefaultCommitTypeList(settings.language().getKey());
                AiGenerationRequest request = new AiGenerationRequest(
                        preferences.getEndpoint(), preferences.getApiPath(), preferences.getModel(),
                        preferences.getTemperature(), preferences.getMaxTokens(), preferences.getSystemPrompt(), settings.language(),
                        allowedTypes, settings.commitMessageRules(),
                        transferMode, changeContent);
                new OpenAiCompatibleProvider().generate(request, new AiCredentials(apiKey), progressIndicator,
                        new Listener(settings));
            }
        });
    }

    private void applySuggestion() {
        try {
            EffectiveCommitTemplateSettings settings = CommitTemplateSettingsResolver.getInstance(project).resolve();
            var suggestion = AiSuggestionParser.parse(response.toString());
            var commit = AiSuggestionValidator.validateAndConvert(suggestion, settings, allowedTypes(settings));
            suggestionConsumer.accept(commit);
            dispose();
        } catch (Exception exception) {
            Messages.showErrorDialog(this, exception.getMessage(), "无法应用 AI 建议");
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
                    output.append("\n\n—— 已完成，可应用到" + applicationTarget + "。——");
                } catch (Exception exception) {
                    output.append("\n\n—— AI 返回内容无法通过本地提交规则校验。——");
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
