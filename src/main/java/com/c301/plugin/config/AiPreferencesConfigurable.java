package com.c301.plugin.config;

import com.c301.plugin.infrastructure.credentials.AiCredentialStore;
import com.c301.plugin.infrastructure.credentials.PasswordSafeAiCredentialStore;
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

/**
 * AI 提交建议的全局配置界面。密钥不进入 Swing 表单状态或持久化 State，
 * 用户提交后立即交给 Password Safe。
 */
final class AiPreferencesConfigurable {
    private final AiPreferencesState state = AiPreferencesState.getInstance();
    private final AiCredentialStore credentialStore = new PasswordSafeAiCredentialStore();
    private JPanel panel;
    private JCheckBox enabled;
    private JTextField endpoint;
    private JTextField apiPath;
    private JTextField model;
    private JSpinner temperature;
    private JSpinner maxTokens;
    private JCheckBox allowDiffTransfer;
    private JTextArea excludePatterns;
    private JLabel credentialStatus;
    private JButton saveKey;
    private JButton clearKey;

    JComponent createComponent() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            panel.setBorder(JBUI.Borders.emptyTop(8));
            GridBagConstraints constraints = constraints(0);

            enabled = new JCheckBox("启用 AI 提交建议");
            panel.add(enabled, constraints);
            constraints.gridy++;
            panel.add(new JLabel("服务协议：OpenAI-Compatible Chat Completions"), constraints);

            endpoint = new JTextField();
            addLabeled("服务地址 Endpoint", endpoint, constraints);
            apiPath = new JTextField();
            addLabeled("API 路径", apiPath, constraints);
            model = new JTextField();
            addLabeled("模型名称", model, constraints);

            temperature = new JSpinner(new SpinnerNumberModel(0.2D, 0.0D, 2.0D, 0.1D));
            addLabeled("Temperature", temperature, constraints);
            maxTokens = new JSpinner(new SpinnerNumberModel(1024, 1, 16384, 1));
            addLabeled("最大输出 Token", maxTokens, constraints);

            constraints.gridy++;
            allowDiffTransfer = new JCheckBox("允许发送经过确认和过滤的 Diff");
            panel.add(allowDiffTransfer, constraints);
            constraints.gridy++;
            JLabel exclusionHint = new JLabel("文件排除规则（每行一条，支持 .gitignore 风格；内置敏感文件规则始终生效）");
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
            saveKey = new JButton("设置 / 替换 API Key");
            clearKey = new JButton("清除 API Key");
            credentials.add(credentialStatus);
            credentials.add(saveKey);
            credentials.add(clearKey);
            panel.add(credentials, constraints);

            enabled.addActionListener(event -> updateOptionsVisibility());
            saveKey.addActionListener(event -> saveApiKey());
            clearKey.addActionListener(event -> clearApiKey());
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
        return panel;
    }

    boolean isModified() {
        return panel != null && (enabled.isSelected() != state.isEnabled()
                || !endpoint.getText().trim().equals(state.getEndpoint())
                || !apiPath.getText().trim().equals(state.getApiPath())
                || !model.getText().trim().equals(state.getModel())
                || Double.compare((Double) temperature.getValue(), state.getTemperature()) != 0
                || !maxTokens.getValue().equals(state.getMaxTokens())
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
        allowDiffTransfer.setSelected(state.isAllowDiffTransfer());
        excludePatterns.setText(String.join("\n", state.getExcludePatterns()));
        refreshCredentialStatus();
        updateOptionsVisibility();
    }

    /** AI 总开关关闭时只保留开关本身，避免配置表单与快捷入口处于可见但不可用的状态。 */
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
            throw new ConfigurationException("AI 服务地址和模型名称不能为空。");
        }
        String value = endpoint.getText().trim().toLowerCase();
        if (!value.startsWith("https://") && !value.startsWith("http://localhost") && !value.startsWith("http://127.0.0.1")) {
            throw new ConfigurationException("远程 AI 服务地址必须使用 HTTPS。");
        }
    }

    private void saveApiKey() {
        String key = Messages.showPasswordDialog(null, "API Key 将保存到 IntelliJ Password Safe，不会写入项目或插件配置文件。",
                "设置 AI API Key", null, null);
        if (key != null && !key.isBlank()) {
            credentialStore.saveApiKey(endpoint.getText().trim(), key.trim());
        }
        refreshCredentialStatus();
    }

    private void clearApiKey() {
        int result = Messages.showYesNoDialog(panel, "确定清除当前服务地址对应的 API Key 吗？", "清除 AI API Key", null);
        if (result == Messages.YES) {
            credentialStore.clearApiKey(endpoint.getText().trim());
        }
        refreshCredentialStatus();
    }

    private void refreshCredentialStatus() {
        if (credentialStatus != null) {
            boolean configured = credentialStore.hasCredential(endpoint == null ? state.getEndpoint() : endpoint.getText().trim());
            credentialStatus.setText(configured ? "API Key：已配置" : "API Key：未配置");
        }
    }

    private List<String> patternsFromEditor() {
        return Arrays.stream(excludePatterns.getText().split("\\R"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
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

    private void addLabeled(String label, JComponent component, GridBagConstraints constraints) {
        constraints.gridy++;
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(new JLabel(label), BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        panel.add(row, constraints);
    }
}
