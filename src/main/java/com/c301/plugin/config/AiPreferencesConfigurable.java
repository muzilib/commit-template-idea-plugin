package com.c301.plugin.config;

import com.c301.plugin.infrastructure.credentials.AiCredentialStore;
import com.c301.plugin.infrastructure.credentials.PasswordSafeAiCredentialStore;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * AI 提交建议的全局配置界面。密钥不进入 Swing 表单状态或持久化 State，
 * 用户提交后立即交给 Password Safe。
 */
final class AiPreferencesConfigurable {
    private final AiPreferencesState state = AiPreferencesState.getInstance();
    private final AiCredentialStore credentialStore = new PasswordSafeAiCredentialStore();
    private JPanel panel;
    private JScrollPane scrollPane;
    private JCheckBox enabled;
    private JTextField endpoint;
    private JTextField apiPath;
    private JTextField model;
    private JSpinner temperature;
    private JSpinner maxTokens;
    private JTextArea systemPrompt;
    private JCheckBox allowDiffTransfer;
    private JTextArea excludePatterns;
    private JLabel credentialStatus;
    private JButton saveKey;
    private JButton clearKey;

    private static ResourceBundle bundle() {
        return CommUtil.i18nResourceBundle(null);
    }

    private static GridBagConstraints constraints(int y) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = y;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = JBUI.insets(4, 0);
        return constraints;
    }

    JComponent createComponent() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            panel.setBorder(JBUI.Borders.emptyTop(8));
            GridBagConstraints constraints = constraints(0);

            ResourceBundle bundle = bundle();
            enabled = new JCheckBox(bundle.getString("plugin.ai.enabled"));
            panel.add(enabled, constraints);
            constraints.gridy++;
            panel.add(new JLabel(bundle.getString("plugin.ai.protocol")), constraints);

            endpoint = new JTextField();
            addLabeled(bundle.getString("plugin.ai.endpoint"), endpoint, constraints);
            apiPath = new JTextField();
            addLabeled(bundle.getString("plugin.ai.apiPath"), apiPath, constraints);
            model = new JTextField();
            addLabeled(bundle.getString("plugin.ai.model"), model, constraints);

            temperature = new JSpinner(new SpinnerNumberModel(0.2D, 0.0D, 2.0D, 0.1D));
            addLabeled(bundle.getString("plugin.ai.temperature"), temperature, constraints);
            maxTokens = new JSpinner(new SpinnerNumberModel(1024, 1, 16384, 1));
            addLabeled(bundle.getString("plugin.ai.maxTokens"), maxTokens, constraints);

            constraints.gridy++;
            panel.add(new JLabel(bundle.getString("plugin.ai.systemPrompt")), constraints);
            constraints.gridy++;
            systemPrompt = new JTextArea(10, 0);
            systemPrompt.setLineWrap(true);
            systemPrompt.setWrapStyleWord(true);
            JScrollPane systemPromptScrollPane = new JScrollPane(systemPrompt);
            systemPromptScrollPane.setMinimumSize(new Dimension(0, JBUI.scale(180)));
            systemPromptScrollPane.setPreferredSize(new Dimension(0, JBUI.scale(200)));
            systemPromptScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            systemPromptScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weighty = 0.5D;
            panel.add(systemPromptScrollPane, constraints);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weighty = 0;

            constraints.gridy++;
            allowDiffTransfer = new JCheckBox(bundle.getString("plugin.ai.allowDiffTransfer"));
            panel.add(allowDiffTransfer, constraints);
            constraints.gridy++;
            JLabel exclusionHint = new JLabel(bundle.getString("plugin.ai.excludePatterns"));
            panel.add(exclusionHint, constraints);
            constraints.gridy++;
            // 排除规则按行编辑，必须保留足够的可见高度，不能因父级 GridBagLayout 压缩为单行。
            excludePatterns = new JTextArea(8, 0);
            excludePatterns.setLineWrap(false);
            excludePatterns.setMinimumSize(new Dimension(0, JBUI.scale(140)));
            JScrollPane patternScrollPane = new JScrollPane(excludePatterns);
            patternScrollPane.setMinimumSize(new Dimension(0, JBUI.scale(140)));
            patternScrollPane.setPreferredSize(new Dimension(0, JBUI.scale(160)));
            patternScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            patternScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weighty = 0.4D;
            panel.add(patternScrollPane, constraints);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weighty = 0;

            constraints.gridy++;
            JPanel credentials = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            credentialStatus = new JLabel();
            saveKey = new JButton(bundle.getString("plugin.ai.saveApiKey"));
            clearKey = new JButton(bundle.getString("plugin.ai.clearApiKey"));
            credentials.add(credentialStatus);
            credentials.add(saveKey);
            credentials.add(clearKey);
            panel.add(credentials, constraints);

            enabled.addActionListener(event -> updateOptionsVisibility());
            saveKey.addActionListener(event -> saveApiKey());
            clearKey.addActionListener(event -> clearApiKey());
            // 滚动视口比表单更高时，容器负责将表单固定在顶部，避免 GridBagLayout 垂直居中。
            JPanel scrollContent = new JPanel(new BorderLayout());
            scrollContent.add(panel, BorderLayout.NORTH);
            scrollPane = new JScrollPane(scrollContent,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(JBUI.Borders.empty());
            scrollPane.getVerticalScrollBar().setUnitIncrement(JBUI.scale(16));

            endpoint.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    refreshCredentialStatus();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    refreshCredentialStatus();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    refreshCredentialStatus();
                }
            });
        }
        reset();
        return scrollPane;
    }

    boolean isModified() {
        return panel != null && (enabled.isSelected() != state.isEnabled()
                || !endpoint.getText().trim().equals(state.getEndpoint())
                || !apiPath.getText().trim().equals(state.getApiPath())
                || !model.getText().trim().equals(state.getModel())
                || Double.compare((Double) temperature.getValue(), state.getTemperature()) != 0
                || !maxTokens.getValue().equals(state.getMaxTokens())
                || !systemPrompt.getText().equals(state.getSystemPrompt())
                || allowDiffTransfer.isSelected() != state.isAllowDiffTransfer()
                || !patternsFromEditor().equals(state.getExcludePatterns()));
    }

    void apply() throws ConfigurationException {
        validateConfiguration();
        state.setEnabled(enabled.isSelected());
        state.setEndpoint(endpoint.getText().trim());
        state.setApiPath(apiPath.getText().trim());
        state.setModel(model.getText().trim());
        state.setTemperature((Double) temperature.getValue());
        state.setMaxTokens((Integer) maxTokens.getValue());
        state.setSystemPrompt(systemPrompt.getText());
        state.setAllowDiffTransfer(allowDiffTransfer.isSelected());
        state.setExcludePatterns(new ArrayList<>(patternsFromEditor()));
    }

    void reset() {
        if (panel == null) {
            return;
        }
        enabled.setSelected(state.isEnabled());
        endpoint.setText(state.getEndpoint());
        apiPath.setText(state.getApiPath());
        model.setText(state.getModel());
        temperature.setValue(state.getTemperature());
        maxTokens.setValue(state.getMaxTokens());
        systemPrompt.setText(state.getSystemPrompt());
        allowDiffTransfer.setSelected(state.isAllowDiffTransfer());
        excludePatterns.setText(String.join("\n", state.getExcludePatterns()));
        refreshCredentialStatus();
        updateOptionsVisibility();
    }

    /**
     * AI 总开关关闭时只保留开关本身，避免配置表单与快捷入口处于可见但不可用的状态。
     */
    private void updateOptionsVisibility() {
        if (panel == null) {
            return;
        }
        for (Component component : panel.getComponents()) {
            if (component != enabled) {
                component.setVisible(enabled.isSelected());
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void validateConfiguration() throws ConfigurationException {
        if (endpoint.getText().isBlank() || model.getText().isBlank()) {
            throw new ConfigurationException(bundle().getString("plugin.ai.error.endpointAndModelRequired"));
        }
        if (systemPrompt.getText().isBlank()) {
            throw new ConfigurationException(bundle().getString("plugin.ai.error.systemPromptRequired"));
        }
        String value = endpoint.getText().trim().toLowerCase();
        if (!value.startsWith("https://") && !value.startsWith("http://localhost") && !value.startsWith("http://127.0.0.1")) {
            throw new ConfigurationException(bundle().getString("plugin.ai.error.httpsRequired"));
        }
    }

    private void saveApiKey() {
        String key = Messages.showPasswordDialog(null, bundle().getString("plugin.ai.apiKeyHint"),
                bundle().getString("plugin.ai.saveApiKeyTitle"), null, null);
        if (key != null && !key.isBlank()) {
            credentialStore.saveApiKey(endpoint.getText().trim(), key.trim());
        }
        refreshCredentialStatus();
    }

    private void clearApiKey() {
        int result = Messages.showYesNoDialog(panel, bundle().getString("plugin.ai.clearApiKeyConfirmation"),
                bundle().getString("plugin.ai.clearApiKeyTitle"), null);
        if (result == Messages.YES) {
            credentialStore.clearApiKey(endpoint.getText().trim());
        }
        refreshCredentialStatus();
    }

    private void refreshCredentialStatus() {
        if (credentialStatus != null) {
            boolean configured = credentialStore.hasCredential(endpoint == null ? state.getEndpoint() : endpoint.getText().trim());
            credentialStatus.setText(bundle().getString(configured ? "plugin.ai.apiKeyConfigured" : "plugin.ai.apiKeyNotConfigured"));
        }
    }

    private List<String> patternsFromEditor() {
        return Arrays.stream(excludePatterns.getText().split("\\R"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private void addLabeled(String label, JComponent component, GridBagConstraints constraints) {
        constraints.gridy++;
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(new JLabel(label), BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        panel.add(row, constraints);
    }
}
