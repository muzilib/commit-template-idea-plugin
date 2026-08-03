package com.c301.plugin.config;

import com.c301.plugin.domain.ai.*;
import com.c301.plugin.infrastructure.ai.AiSystemPromptTemplates;
import com.c301.plugin.infrastructure.credentials.AiCredentialStore;
import com.c301.plugin.infrastructure.credentials.PasswordSafeAiCredentialStore;
import com.c301.plugin.utils.CommUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

/**
 * AI 提交建议的全局配置界面。密钥只保存到 Password Safe，系统提示词可按供应商单独覆盖。
 */
final class AiPreferencesConfigurable {
    private final AiPreferencesState state = AiPreferencesState.getInstance();
    private final AiCredentialStore credentialStore = new PasswordSafeAiCredentialStore();
    private JPanel panel;
    private JScrollPane scrollPane;
    private JCheckBox enabled;
    private JCheckBox checkDiffBeforeSending;
    private JCheckBox showAdvancedSettings;
    private JPanel advancedSettings;
    private JComboBox<AiProviderType> providerType;
    private JTextField apiUrl;
    private JTextField model;
    private JLabel protocolLabel;
    private JLabel providerLabel;
    private JLabel apiUrlLabel;
    private JLabel modelLabel;
    private JLabel excludePatternsLabel;
    private JLabel temperatureLabel;
    private JLabel maxTokensLabel;
    private JLabel systemPromptLabel;
    private JLabel providerHint;
    private JSpinner temperature;
    private JSpinner maxTokens;
    private JTextArea systemPrompt;
    private JTextArea excludePatterns;
    private JLabel credentialStatus;
    private JButton saveKey;
    private JButton clearKey;
    private JLabel apiKeyHelp;
    private JPopupMenu apiKeyHelpPopup;
    private javax.swing.Timer apiKeyHelpCloseTimer;
    private JButton restorePrompt;
    private JPanel qwenSettings;
    private JLabel qwenSamplingLabel;
    private JLabel qwenThinkingLabel;
    private JLabel qwenSearchLabel;
    private TitledBorder qwenSettingsBorder;
    private JLabel qwenTopPLabel;
    private JLabel qwenTopKLabel;
    private JLabel qwenRepetitionPenaltyLabel;
    private JLabel qwenPresencePenaltyLabel;
    private JLabel qwenSeedLabel;
    private JLabel qwenThinkingBudgetLabel;
    private JLabel qwenReasoningEffortLabel;
    private JLabel qwenSearchStrategyLabel;
    private JCheckBox qwenIncludeUsage;
    private JTextField qwenTopP;
    private JTextField qwenTopK;
    private JTextField qwenRepetitionPenalty;
    private JTextField qwenPresencePenalty;
    private JTextField qwenSeed;
    private JCheckBox qwenEnableThinking;
    private JTextField qwenThinkingBudget;
    private JComboBox<String> qwenReasoningEffort;
    private JCheckBox qwenEnableSearch;
    private JCheckBox qwenForceSearch;
    private JComboBox<String> qwenSearchStrategy;
    private JCheckBox qwenDataInspection;
    private JPanel deepSeekSettings;
    private JLabel deepSeekTopPLabel;
    private JLabel deepSeekReasoningEffortLabel;
    private TitledBorder deepSeekSettingsBorder;
    private JCheckBox deepSeekIncludeUsage;
    private JTextField deepSeekTopP;
    private JCheckBox deepSeekEnableThinking;
    private JComboBox<String> deepSeekReasoningEffort;
    private JPanel openAiSettings;
    private JLabel openAiReasoningEffortLabel;
    private JLabel openAiVerbosityLabel;
    private JLabel openAiTopPLabel;
    private TitledBorder openAiSettingsBorder;
    private JComboBox<String> openAiReasoningEffort;
    private JComboBox<String> openAiVerbosity;
    private JTextField openAiTopP;
    private JCheckBox openAiStoreResponse;
    private AiProviderType displayedProvider;
    private String customApiUrl = "";
    private String customModel = "";
    private boolean resetting;
    private Map<AiProviderType, String> draftSystemPrompts = new LinkedHashMap<>();

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

    private static Map<AiProviderType, String> normalizePrompts(Map<AiProviderType, String> prompts) {
        Map<AiProviderType, String> result = new LinkedHashMap<>();
        if (prompts != null) {
            prompts.forEach((provider, prompt) -> {
                if (provider != null && prompt != null && !prompt.isBlank()) {
                    result.put(provider, prompt.trim());
                }
            });
        }
        return result;
    }

    private static JLabel addAdvancedLabeled(JPanel panel, String label, JComponent component, GridBagConstraints constraints) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JLabel labelComponent = new JLabel(label);
        row.add(labelComponent, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        panel.add(row, constraints);
        return labelComponent;
    }

    private static String formatOptional(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String selectedOptional(JComboBox<String> comboBox) {
        String value = (String) comboBox.getSelectedItem();
        return value == null || value.isBlank() ? null : value;
    }

    private static Double optionalDouble(JTextField field) {
        try {
            return requiredOptionalDouble(field);
        } catch (ConfigurationException ignored) {
            return null;
        }
    }

    private static Integer optionalInteger(JTextField field) {
        try {
            return requiredOptionalInteger(field);
        } catch (ConfigurationException ignored) {
            return null;
        }
    }

    private static Long optionalLong(JTextField field) {
        try {
            return requiredOptionalLong(field);
        } catch (ConfigurationException ignored) {
            return null;
        }
    }

    private static Double requiredOptionalDouble(JTextField field) throws ConfigurationException {
        String value = field.getText().trim();
        if (value.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationException(bundle().getString("plugin.ai.qwen.error.invalidOption"));
        }
    }

    private static Integer requiredOptionalInteger(JTextField field) throws ConfigurationException {
        String value = field.getText().trim();
        if (value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationException(bundle().getString("plugin.ai.qwen.error.invalidOption"));
        }
    }

    private static Long requiredOptionalLong(JTextField field) throws ConfigurationException {
        String value = field.getText().trim();
        if (value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationException(bundle().getString("plugin.ai.qwen.error.invalidOption"));
        }
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
            checkDiffBeforeSending = new JCheckBox(bundle.getString("plugin.ai.checkDiffBeforeSending"));
            panel.add(checkDiffBeforeSending, constraints);
            constraints.gridy++;
            protocolLabel = new JLabel(bundle.getString("plugin.ai.protocol"));
            panel.add(protocolLabel, constraints);

            providerType = new JComboBox<>(AiProviderType.values());
            providerType.setRenderer(new ProviderRenderer());
            providerLabel = addLabeled(bundle.getString("plugin.ai.providerType"), providerType, constraints);

            apiUrl = new JTextField();
            apiUrlLabel = addLabeled(bundle.getString("plugin.ai.apiUrl"), apiUrl, constraints);
            providerHint = new JLabel();
            providerHint.setBorder(JBUI.Borders.emptyLeft(4));
            constraints.gridy++;
            panel.add(providerHint, constraints);

            model = new JTextField();
            modelLabel = addLabeled(bundle.getString("plugin.ai.model"), model, constraints);

            JPanel credentials = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            credentialStatus = new JLabel();
            saveKey = new JButton(bundle.getString("plugin.ai.saveApiKey"));
            clearKey = new JButton(bundle.getString("plugin.ai.clearApiKey"));
            credentials.add(credentialStatus);
            credentials.add(saveKey);
            credentials.add(clearKey);
            apiKeyHelp = createApiKeyHelp(bundle);
            credentials.add(apiKeyHelp);
            constraints.gridy++;
            panel.add(credentials, constraints);

            constraints.gridy++;
            showAdvancedSettings = new JCheckBox(bundle.getString("plugin.ai.moreSettings"));
            panel.add(showAdvancedSettings, constraints);
            constraints.gridy++;
            advancedSettings = createAdvancedSettings(bundle);
            panel.add(advancedSettings, constraints);

            constraints.gridy++;
            excludePatternsLabel = new JLabel(bundle.getString("plugin.ai.excludePatterns"));
            panel.add(excludePatternsLabel, constraints);
            constraints.gridy++;
            // 排除规则按行编辑，必须保留足够的可见高度，不能因父级 GridBagLayout 压缩为单行。
            excludePatterns = new JTextArea(8, 0);
            excludePatterns.setLineWrap(false);
            JScrollPane patternScrollPane = new JScrollPane(excludePatterns);
            patternScrollPane.setMinimumSize(new Dimension(0, JBUI.scale(140)));
            patternScrollPane.setPreferredSize(new Dimension(0, JBUI.scale(160)));
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weighty = 0.4D;
            panel.add(patternScrollPane, constraints);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weighty = 0;

            enabled.addActionListener(event -> updateOptionsVisibility());
            showAdvancedSettings.addActionListener(event -> updateAdvancedSettingsVisibility());
            providerType.addActionListener(event -> {
                if (!resetting) {
                    switchProvider();
                }
            });
            saveKey.addActionListener(event -> saveApiKey());
            clearKey.addActionListener(event -> clearApiKey());
            restorePrompt.addActionListener(event -> restoreDefaultPrompt());
            qwenEnableThinking.addActionListener(event -> updateQwenDependentOptions());
            qwenEnableSearch.addActionListener(event -> updateQwenDependentOptions());
            deepSeekEnableThinking.addActionListener(event -> updateDeepSeekDependentOptions());
            apiUrl.getDocument().addDocumentListener(new SimpleDocumentListener(this::refreshCredentialStatus));

            JPanel scrollContent = new JPanel(new BorderLayout());
            scrollContent.add(panel, BorderLayout.NORTH);
            scrollPane = new JScrollPane(scrollContent,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(JBUI.Borders.empty());
            scrollPane.getVerticalScrollBar().setUnitIncrement(JBUI.scale(16));
        }
        reset();
        return scrollPane;
    }

    void refreshLanguage() {
        if (panel == null) {
            return;
        }
        ResourceBundle bundle = bundle();
        protocolLabel.setText(bundle.getString("plugin.ai.protocol"));
        providerLabel.setText(bundle.getString("plugin.ai.providerType"));
        apiUrlLabel.setText(bundle.getString("plugin.ai.apiUrl"));
        modelLabel.setText(bundle.getString("plugin.ai.model"));
        excludePatternsLabel.setText(bundle.getString("plugin.ai.excludePatterns"));
        enabled.setText(bundle.getString("plugin.ai.enabled"));
        checkDiffBeforeSending.setText(bundle.getString("plugin.ai.checkDiffBeforeSending"));
        saveKey.setText(bundle.getString("plugin.ai.saveApiKey"));
        clearKey.setText(bundle.getString("plugin.ai.clearApiKey"));
        showAdvancedSettings.setText(bundle.getString("plugin.ai.moreSettings"));
        apiKeyHelp.setToolTipText(bundle.getString("plugin.ai.apiKeyHelp.accessibleDescription"));
        temperatureLabel.setText(bundle.getString("plugin.ai.temperature"));
        maxTokensLabel.setText(bundle.getString("plugin.ai.maxTokens"));
        systemPromptLabel.setText(bundle.getString("plugin.ai.systemPrompt"));
        restorePrompt.setText(bundle.getString("plugin.ai.restoreDefaultPrompt"));
        qwenSettingsBorder.setTitle(bundle.getString("plugin.ai.qwen.settings"));
        qwenIncludeUsage.setText(bundle.getString("plugin.ai.qwen.includeUsage"));
        qwenSamplingLabel.setText(bundle.getString("plugin.ai.qwen.sampling"));
        qwenTopPLabel.setText(bundle.getString("plugin.ai.qwen.topP"));
        qwenTopKLabel.setText(bundle.getString("plugin.ai.qwen.topK"));
        qwenRepetitionPenaltyLabel.setText(bundle.getString("plugin.ai.qwen.repetitionPenalty"));
        qwenPresencePenaltyLabel.setText(bundle.getString("plugin.ai.qwen.presencePenalty"));
        qwenSeedLabel.setText(bundle.getString("plugin.ai.qwen.seed"));
        qwenThinkingLabel.setText(bundle.getString("plugin.ai.qwen.thinking"));
        qwenEnableThinking.setText(bundle.getString("plugin.ai.qwen.enableThinking"));
        qwenThinkingBudgetLabel.setText(bundle.getString("plugin.ai.qwen.thinkingBudget"));
        qwenReasoningEffortLabel.setText(bundle.getString("plugin.ai.qwen.reasoningEffort"));
        qwenSearchLabel.setText(bundle.getString("plugin.ai.qwen.search"));
        qwenEnableSearch.setText(bundle.getString("plugin.ai.qwen.enableSearch"));
        qwenForceSearch.setText(bundle.getString("plugin.ai.qwen.forceSearch"));
        qwenSearchStrategyLabel.setText(bundle.getString("plugin.ai.qwen.searchStrategy"));
        qwenDataInspection.setText(bundle.getString("plugin.ai.qwen.dataInspection"));
        qwenTopP.putClientProperty("JTextField.placeholderText", bundle.getString("plugin.ai.qwen.defaultOptional"));
        qwenTopK.putClientProperty("JTextField.placeholderText", bundle.getString("plugin.ai.qwen.defaultOptional"));
        qwenRepetitionPenalty.putClientProperty("JTextField.placeholderText", bundle.getString("plugin.ai.qwen.defaultOptional"));
        qwenPresencePenalty.putClientProperty("JTextField.placeholderText", bundle.getString("plugin.ai.qwen.defaultOptional"));
        qwenSeed.putClientProperty("JTextField.placeholderText", bundle.getString("plugin.ai.qwen.randomOptional"));
        qwenThinkingBudget.putClientProperty("JTextField.placeholderText", bundle.getString("plugin.ai.qwen.defaultOptional"));
        deepSeekSettingsBorder.setTitle(bundle.getString("plugin.ai.deepseek.settings"));
        deepSeekIncludeUsage.setText(bundle.getString("plugin.ai.deepseek.includeUsage"));
        deepSeekTopPLabel.setText(bundle.getString("plugin.ai.deepseek.topP"));
        deepSeekEnableThinking.setText(bundle.getString("plugin.ai.deepseek.enableThinking"));
        deepSeekReasoningEffortLabel.setText(bundle.getString("plugin.ai.deepseek.reasoningEffort"));
        openAiSettingsBorder.setTitle(bundle.getString("plugin.ai.openai.settings"));
        openAiReasoningEffortLabel.setText(bundle.getString("plugin.ai.openai.reasoningEffort"));
        openAiVerbosityLabel.setText(bundle.getString("plugin.ai.openai.verbosity"));
        openAiTopPLabel.setText(bundle.getString("plugin.ai.openai.topP"));
        deepSeekTopP.putClientProperty("JTextField.placeholderText", bundle.getString("plugin.ai.qwen.defaultOptional"));
        openAiTopP.putClientProperty("JTextField.placeholderText", bundle.getString("plugin.ai.qwen.defaultOptional"));
        openAiStoreResponse.setText(bundle.getString("plugin.ai.openai.storeResponse"));
        providerType.repaint();
        panel.revalidate();
        panel.repaint();
        refreshProviderUi(selectedProvider());
    }

    boolean isModified() {
        return panel != null && (enabled.isSelected() != state.isEnabled()
                || checkDiffBeforeSending.isSelected() != state.isCheckDiffBeforeSending()
                || showAdvancedSettings.isSelected() != state.isShowAdvancedSettings()
                || selectedProvider() != state.getProviderType()
                || !apiUrl.getText().trim().equals(state.getApiUrl())
                || !model.getText().trim().equals(state.getModel())
                || Double.compare((Double) temperature.getValue(), state.getTemperature()) != 0
                || !maxTokens.getValue().equals(state.getMaxTokens())
                || !qwenOptionsFromEditor().equals(state.getQwenGenerationOptions())
                || !deepSeekOptionsFromEditor().equals(state.getDeepSeekGenerationOptions())
                || !openAiOptionsFromEditor().equals(state.getOpenAiGenerationOptions())
                || !draftPromptsWithCurrentEditor().equals(normalizePrompts(state.getCustomSystemPrompts()))
                || !patternsFromEditor().equals(state.getExcludePatterns()));
    }

    void apply() throws ConfigurationException {
        validateConfiguration();
        boolean wasEnabled = state.isEnabled();
        state.setEnabled(enabled.isSelected());
        if (!wasEnabled && enabled.isSelected() && state.getDataTransferConsent() == AiDataTransferConsent.DECLINED) {
            state.setDataTransferConsent(AiDataTransferConsent.UNDECIDED);
        }
        state.setCheckDiffBeforeSending(checkDiffBeforeSending.isSelected());
        state.setShowAdvancedSettings(showAdvancedSettings.isSelected());
        state.setProviderType(selectedProvider());
        state.setApiUrl(apiUrl.getText().trim());
        state.setModel(model.getText().trim());
        state.setTemperature((Double) temperature.getValue());
        state.setMaxTokens((Integer) maxTokens.getValue());
        state.setQwenGenerationOptions(qwenOptionsFromEditor());
        state.setDeepSeekGenerationOptions(deepSeekOptionsFromEditor());
        state.setOpenAiGenerationOptions(openAiOptionsFromEditor());
        savePromptFor(selectedProvider());
        state.setCustomSystemPrompts(normalizePrompts(draftSystemPrompts));
        state.setExcludePatterns(new ArrayList<>(patternsFromEditor()));
    }

    void reset() {
        if (panel == null) {
            return;
        }
        enabled.setSelected(state.isEnabled());
        checkDiffBeforeSending.setSelected(state.isCheckDiffBeforeSending());
        showAdvancedSettings.setSelected(state.isShowAdvancedSettings());
        displayedProvider = null;
        customApiUrl = state.getProviderType() == AiProviderType.CUSTOM ? state.getApiUrl() : "";
        customModel = state.getProviderType() == AiProviderType.CUSTOM ? state.getModel() : "";
        draftSystemPrompts = normalizePrompts(state.getCustomSystemPrompts());
        resetting = true;
        providerType.setSelectedItem(state.getProviderType());
        resetting = false;
        model.setText(state.getModel());
        temperature.setValue(state.getTemperature());
        maxTokens.setValue(state.getMaxTokens());
        resetQwenOptions(state.getQwenGenerationOptions());
        resetDeepSeekOptions(state.getDeepSeekGenerationOptions());
        resetOpenAiOptions(state.getOpenAiGenerationOptions());
        excludePatterns.setText(String.join("\n", state.getExcludePatterns()));
        // 重置时必须恢复当前供应商已保存的地址和模型，不能套用切换时的推荐默认值。
        apiUrl.setText(state.getApiUrl());
        AiProviderType provider = selectedProvider();
        displayedProvider = provider;
        refreshProviderUi(provider);
        refreshCredentialStatus();
        updateAdvancedSettingsVisibility();
        updateOptionsVisibility();
    }

    private void switchProvider() {
        if (systemPrompt == null) {
            return;
        }
        savePromptFor(displayedProvider);
        if (displayedProvider == AiProviderType.CUSTOM) {
            customApiUrl = apiUrl.getText().trim();
            customModel = model.getText().trim();
        }
        AiProviderType provider = selectedProvider();
        displayedProvider = provider;
        if (provider.usesPresetApiUrl()) {
            apiUrl.setText(provider.apiUrl());
            model.setText(provider.defaultModel());
        } else {
            apiUrl.setText(customApiUrl);
            model.setText(customModel);
        }
        refreshProviderUi(provider);
    }

    private void refreshProviderUi(AiProviderType provider) {
        providerHint.setText(bundle().getString(provider.usesPresetApiUrl()
                ? "plugin.ai.providerApiKeyHint" : "plugin.ai.customProviderHint"));
        apiUrl.setEditable(true);
        model.putClientProperty("JTextField.placeholderText", bundle().getString(provider.modelPlaceholderKey()));
        systemPrompt.setText(promptFor(provider));
        updateQwenSettingsVisibility();
        updateDeepSeekSettingsVisibility();
        updateOpenAiSettingsVisibility();
        updateApiKeyHelpVisibility();
        refreshCredentialStatus();
    }

    private JLabel createApiKeyHelp(ResourceBundle bundle) {
        JLabel help = new JLabel(AllIcons.General.ContextHelp);
        help.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        help.setToolTipText(bundle.getString("plugin.ai.apiKeyHelp.accessibleDescription"));
        help.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                showApiKeyHelp();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                scheduleApiKeyHelpClose();
            }
        });
        return help;
    }

    private void updateApiKeyHelpVisibility() {
        if (apiKeyHelp == null) {
            return;
        }
        boolean visible = selectedProvider().hasApiKeyHelp();
        apiKeyHelp.setVisible(visible);
        if (!visible) {
            hideApiKeyHelp();
        }
    }

    private void showApiKeyHelp() {
        AiProviderType provider = selectedProvider();
        if (apiKeyHelp == null || !apiKeyHelp.isShowing() || !provider.hasApiKeyHelp()) {
            return;
        }
        cancelApiKeyHelpClose();
        hideApiKeyHelp();
        apiKeyHelpPopup = createApiKeyHelpPopup(bundle(), provider);
        apiKeyHelpPopup.show(apiKeyHelp, apiKeyHelp.getWidth(), 0);
    }

    private JPopupMenu createApiKeyHelpPopup(ResourceBundle bundle, AiProviderType provider) {
        JPopupMenu popup = new JPopupMenu();
        popup.setFocusable(false);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(JBUI.Borders.empty(10, 12));
        String message = bundle.getString(provider.apiKeyHelpMessageKey());
        for (String line : message.split("\\n")) {
            content.add(new JLabel(line));
        }
        if (provider.hasApiKeyHelpLink()) {
            content.add(Box.createVerticalStrut(JBUI.scale(8)));
            JLabel link = new JLabel("<html><a href='#'>" + bundle.getString(provider.apiKeyHelpLinkTextKey()) + "</a></html>");
            link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            link.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    openApiKeyHelpPage(provider);
                }
            });
            content.add(link);
        }
        MouseAdapter hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                cancelApiKeyHelpClose();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                scheduleApiKeyHelpClose();
            }
        };
        content.addMouseListener(hoverListener);
        for (Component component : content.getComponents()) {
            component.addMouseListener(hoverListener);
        }
        popup.add(content);
        return popup;
    }

    private void scheduleApiKeyHelpClose() {
        if (apiKeyHelpCloseTimer == null) {
            apiKeyHelpCloseTimer = new javax.swing.Timer(250, event -> hideApiKeyHelp());
            apiKeyHelpCloseTimer.setRepeats(false);
        }
        apiKeyHelpCloseTimer.restart();
    }

    private void cancelApiKeyHelpClose() {
        if (apiKeyHelpCloseTimer != null) {
            apiKeyHelpCloseTimer.stop();
        }
    }

    private void hideApiKeyHelp() {
        cancelApiKeyHelpClose();
        if (apiKeyHelpPopup != null) {
            apiKeyHelpPopup.setVisible(false);
        }
    }

    private void openApiKeyHelpPage(AiProviderType provider) {
        try {
            Desktop.getDesktop().browse(new URI(provider.apiKeyHelpUrl()));
        } catch (IOException | URISyntaxException | UnsupportedOperationException exception) {
            Messages.showErrorDialog(panel, bundle().getString("plugin.ai.apiKeyHelp.openLinkFailure"),
                    bundle().getString("plugin.ai.apiKeyHelp.openLinkFailureTitle"));
        }
    }

    private void restoreDefaultPrompt() {
        AiProviderType provider = selectedProvider();
        systemPrompt.setText(AiSystemPromptTemplates.forProvider(provider));
    }

    private void savePromptFor(AiProviderType provider) {
        if (provider == null || systemPrompt == null) {
            return;
        }
        String current = systemPrompt.getText().trim();
        String defaultPrompt = AiSystemPromptTemplates.forProvider(provider);
        if (current.isBlank() || current.equals(defaultPrompt)) {
            draftSystemPrompts.remove(provider);
        } else {
            draftSystemPrompts.put(provider, current);
        }
    }

    private String promptFor(AiProviderType provider) {
        String custom = draftSystemPrompts.get(provider);
        return custom == null || custom.isBlank() ? AiSystemPromptTemplates.forProvider(provider) : custom;
    }

    private JPanel createAdvancedSettings(ResourceBundle bundle) {
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = constraints(0);
        constraints.insets = JBUI.insetsBottom(4);
        temperature = new JSpinner(new SpinnerNumberModel(0.2D, 0.0D, 2.0D, 0.1D));
        temperatureLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.temperature"), temperature, constraints);
        constraints.gridy++;
        maxTokens = new JSpinner(new SpinnerNumberModel(1024, 1, 16384, 1));
        maxTokensLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.maxTokens"), maxTokens, constraints);
        constraints.gridy++;
        qwenSettings = createQwenSettings(bundle);
        content.add(qwenSettings, constraints);
        constraints.gridy++;
        deepSeekSettings = createDeepSeekSettings(bundle);
        content.add(deepSeekSettings, constraints);
        constraints.gridy++;
        openAiSettings = createOpenAiSettings(bundle);
        content.add(openAiSettings, constraints);
        constraints.gridy++;
        systemPromptLabel = new JLabel(bundle.getString("plugin.ai.systemPrompt"));
        content.add(systemPromptLabel, constraints);
        constraints.gridy++;
        systemPrompt = new JTextArea(10, 0);
        systemPrompt.setLineWrap(true);
        systemPrompt.setWrapStyleWord(true);
        JScrollPane promptScroll = new JScrollPane(systemPrompt);
        promptScroll.setMinimumSize(new Dimension(0, JBUI.scale(180)));
        promptScroll.setPreferredSize(new Dimension(0, JBUI.scale(200)));
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        content.add(promptScroll, constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 0;
        constraints.gridy++;
        restorePrompt = new JButton(bundle.getString("plugin.ai.restoreDefaultPrompt"));
        JPanel promptActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        promptActions.add(restorePrompt);
        content.add(promptActions, constraints);
        return content;
    }

    private JPanel createQwenSettings(ResourceBundle bundle) {
        JPanel content = new JPanel(new GridBagLayout());
        qwenSettingsBorder = BorderFactory.createTitledBorder(bundle.getString("plugin.ai.qwen.settings"));
        content.setBorder(qwenSettingsBorder);
        GridBagConstraints constraints = constraints(0);
        constraints.insets = JBUI.insetsBottom(4);
        qwenIncludeUsage = new JCheckBox(bundle.getString("plugin.ai.qwen.includeUsage"));
        content.add(qwenIncludeUsage, constraints);
        constraints.gridy++;
        qwenSamplingLabel = new JLabel(bundle.getString("plugin.ai.qwen.sampling"));
        content.add(qwenSamplingLabel, constraints);
        constraints.gridy++;
        qwenTopP = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        qwenTopPLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.topP"), qwenTopP, constraints);
        constraints.gridy++;
        qwenTopK = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        qwenTopKLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.topK"), qwenTopK, constraints);
        constraints.gridy++;
        qwenRepetitionPenalty = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        qwenRepetitionPenaltyLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.repetitionPenalty"), qwenRepetitionPenalty, constraints);
        constraints.gridy++;
        qwenPresencePenalty = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        qwenPresencePenaltyLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.presencePenalty"), qwenPresencePenalty, constraints);
        constraints.gridy++;
        qwenSeed = optionalField(bundle, "plugin.ai.qwen.randomOptional");
        qwenSeedLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.seed"), qwenSeed, constraints);
        constraints.gridy++;
        qwenThinkingLabel = new JLabel(bundle.getString("plugin.ai.qwen.thinking"));
        content.add(qwenThinkingLabel, constraints);
        constraints.gridy++;
        qwenEnableThinking = new JCheckBox(bundle.getString("plugin.ai.qwen.enableThinking"));
        content.add(qwenEnableThinking, constraints);
        constraints.gridy++;
        qwenThinkingBudget = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        qwenThinkingBudgetLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.thinkingBudget"), qwenThinkingBudget, constraints);
        constraints.gridy++;
        qwenReasoningEffort = new JComboBox<>(new String[]{"", "low", "medium", "high", "xhigh", "max"});
        qwenReasoningEffortLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.reasoningEffort"), qwenReasoningEffort, constraints);
        constraints.gridy++;
        qwenSearchLabel = new JLabel(bundle.getString("plugin.ai.qwen.search"));
        content.add(qwenSearchLabel, constraints);
        constraints.gridy++;
        qwenEnableSearch = new JCheckBox(bundle.getString("plugin.ai.qwen.enableSearch"));
        content.add(qwenEnableSearch, constraints);
        constraints.gridy++;
        qwenForceSearch = new JCheckBox(bundle.getString("plugin.ai.qwen.forceSearch"));
        content.add(qwenForceSearch, constraints);
        constraints.gridy++;
        qwenSearchStrategy = new JComboBox<>(new String[]{"turbo", "max", "agent", "agent_max"});
        qwenSearchStrategyLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.searchStrategy"), qwenSearchStrategy, constraints);
        constraints.gridy++;
        qwenDataInspection = new JCheckBox(bundle.getString("plugin.ai.qwen.dataInspection"));
        content.add(qwenDataInspection, constraints);
        return content;
    }

    private JTextField optionalField(ResourceBundle bundle, String placeholderKey) {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", bundle.getString(placeholderKey));
        return field;
    }

    private void updateQwenSettingsVisibility() {
        if (qwenSettings == null) {
            return;
        }
        qwenSettings.setVisible(showAdvancedSettings.isSelected() && selectedProvider() == AiProviderType.QWEN);
        updateQwenDependentOptions();
    }

    private void updateQwenDependentOptions() {
        if (qwenEnableThinking == null) {
            return;
        }
        boolean thinkingEnabled = qwenEnableThinking.isSelected();
        qwenThinkingBudget.setEnabled(thinkingEnabled);
        qwenReasoningEffort.setEnabled(thinkingEnabled);
        boolean searchEnabled = qwenEnableSearch.isSelected();
        qwenForceSearch.setEnabled(searchEnabled);
        qwenSearchStrategy.setEnabled(searchEnabled);
    }

    private void resetQwenOptions(QwenGenerationOptions options) {
        QwenGenerationOptions value = options == null ? new QwenGenerationOptions() : options;
        qwenIncludeUsage.setSelected(value.isIncludeUsage());
        qwenTopP.setText(formatOptional(value.getTopP()));
        qwenTopK.setText(formatOptional(value.getTopK()));
        qwenRepetitionPenalty.setText(formatOptional(value.getRepetitionPenalty()));
        qwenPresencePenalty.setText(formatOptional(value.getPresencePenalty()));
        qwenSeed.setText(formatOptional(value.getSeed()));
        qwenEnableThinking.setSelected(value.isEnableThinking());
        qwenThinkingBudget.setText(formatOptional(value.getThinkingBudget()));
        qwenReasoningEffort.setSelectedItem(value.getReasoningEffort() == null ? "" : value.getReasoningEffort());
        qwenEnableSearch.setSelected(value.isEnableSearch());
        qwenForceSearch.setSelected(value.isForceSearch());
        qwenSearchStrategy.setSelectedItem(value.getSearchStrategy() == null ? "turbo" : value.getSearchStrategy());
        qwenDataInspection.setSelected(value.isDataInspectionEnabled());
        updateQwenDependentOptions();
    }

    private QwenGenerationOptions qwenOptionsFromEditor() {
        QwenGenerationOptions options = new QwenGenerationOptions();
        options.setIncludeUsage(qwenIncludeUsage.isSelected());
        options.setTopP(optionalDouble(qwenTopP));
        options.setTopK(optionalInteger(qwenTopK));
        options.setRepetitionPenalty(optionalDouble(qwenRepetitionPenalty));
        options.setPresencePenalty(optionalDouble(qwenPresencePenalty));
        options.setSeed(optionalLong(qwenSeed));
        options.setEnableThinking(qwenEnableThinking.isSelected());
        options.setThinkingBudget(optionalInteger(qwenThinkingBudget));
        options.setReasoningEffort(selectedOptional(qwenReasoningEffort));
        options.setEnableSearch(qwenEnableSearch.isSelected());
        options.setForceSearch(qwenForceSearch.isSelected());
        options.setSearchStrategy((String) qwenSearchStrategy.getSelectedItem());
        options.setDataInspectionEnabled(qwenDataInspection.isSelected());
        return options;
    }

    private JPanel createDeepSeekSettings(ResourceBundle bundle) {
        JPanel content = new JPanel(new GridBagLayout());
        deepSeekSettingsBorder = BorderFactory.createTitledBorder(bundle.getString("plugin.ai.deepseek.settings"));
        content.setBorder(deepSeekSettingsBorder);
        GridBagConstraints constraints = constraints(0);
        constraints.insets = JBUI.insetsBottom(4);
        deepSeekIncludeUsage = new JCheckBox(bundle.getString("plugin.ai.deepseek.includeUsage"));
        content.add(deepSeekIncludeUsage, constraints);
        constraints.gridy++;
        deepSeekTopP = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        deepSeekTopPLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.deepseek.topP"), deepSeekTopP, constraints);
        constraints.gridy++;
        deepSeekEnableThinking = new JCheckBox(bundle.getString("plugin.ai.deepseek.enableThinking"));
        content.add(deepSeekEnableThinking, constraints);
        constraints.gridy++;
        deepSeekReasoningEffort = new JComboBox<>(new String[]{"low", "high", "max"});
        deepSeekReasoningEffortLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.deepseek.reasoningEffort"), deepSeekReasoningEffort, constraints);
        return content;
    }

    private void updateDeepSeekSettingsVisibility() {
        if (deepSeekSettings == null) {
            return;
        }
        deepSeekSettings.setVisible(showAdvancedSettings.isSelected() && selectedProvider() == AiProviderType.DEEPSEEK);
        updateDeepSeekDependentOptions();
    }

    private void updateDeepSeekDependentOptions() {
        if (deepSeekEnableThinking != null) {
            deepSeekReasoningEffort.setEnabled(deepSeekEnableThinking.isSelected());
        }
    }

    private void resetDeepSeekOptions(DeepSeekGenerationOptions options) {
        DeepSeekGenerationOptions value = options == null ? new DeepSeekGenerationOptions() : options;
        deepSeekIncludeUsage.setSelected(value.isIncludeUsage());
        deepSeekTopP.setText(formatOptional(value.getTopP()));
        deepSeekEnableThinking.setSelected(value.isEnableThinking());
        deepSeekReasoningEffort.setSelectedItem(value.getReasoningEffort() == null ? "high" : value.getReasoningEffort());
        updateDeepSeekDependentOptions();
    }

    private DeepSeekGenerationOptions deepSeekOptionsFromEditor() {
        DeepSeekGenerationOptions options = new DeepSeekGenerationOptions();
        options.setIncludeUsage(deepSeekIncludeUsage.isSelected());
        options.setTopP(optionalDouble(deepSeekTopP));
        options.setEnableThinking(deepSeekEnableThinking.isSelected());
        options.setReasoningEffort((String) deepSeekReasoningEffort.getSelectedItem());
        return options;
    }

    private JPanel createOpenAiSettings(ResourceBundle bundle) {
        JPanel content = new JPanel(new GridBagLayout());
        openAiSettingsBorder = BorderFactory.createTitledBorder(bundle.getString("plugin.ai.openai.settings"));
        content.setBorder(openAiSettingsBorder);
        GridBagConstraints constraints = constraints(0);
        constraints.insets = JBUI.insetsBottom(4);
        openAiReasoningEffort = new JComboBox<>(new String[]{"none", "minimal", "low", "medium", "high", "xhigh", "max"});
        openAiReasoningEffortLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.openai.reasoningEffort"), openAiReasoningEffort, constraints);
        constraints.gridy++;
        openAiVerbosity = new JComboBox<>(new String[]{"low", "medium", "high"});
        openAiVerbosityLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.openai.verbosity"), openAiVerbosity, constraints);
        constraints.gridy++;
        openAiTopP = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        openAiTopPLabel = addAdvancedLabeled(content, bundle.getString("plugin.ai.openai.topP"), openAiTopP, constraints);
        constraints.gridy++;
        openAiStoreResponse = new JCheckBox(bundle.getString("plugin.ai.openai.storeResponse"));
        content.add(openAiStoreResponse, constraints);
        return content;
    }

    private void updateOpenAiSettingsVisibility() {
        if (openAiSettings != null) {
            openAiSettings.setVisible(showAdvancedSettings.isSelected() && selectedProvider() == AiProviderType.CHATGPT);
        }
    }

    private void resetOpenAiOptions(OpenAiGenerationOptions options) {
        OpenAiGenerationOptions value = options == null ? new OpenAiGenerationOptions() : options;
        openAiReasoningEffort.setSelectedItem(value.getReasoningEffort() == null ? "low" : value.getReasoningEffort());
        openAiVerbosity.setSelectedItem(value.getVerbosity() == null ? "low" : value.getVerbosity());
        openAiTopP.setText(formatOptional(value.getTopP()));
        openAiStoreResponse.setSelected(value.isStoreResponse());
    }

    private OpenAiGenerationOptions openAiOptionsFromEditor() {
        OpenAiGenerationOptions options = new OpenAiGenerationOptions();
        options.setReasoningEffort((String) openAiReasoningEffort.getSelectedItem());
        options.setVerbosity((String) openAiVerbosity.getSelectedItem());
        options.setTopP(optionalDouble(openAiTopP));
        options.setStoreResponse(openAiStoreResponse.isSelected());
        return options;
    }

    private void updateAdvancedSettingsVisibility() {
        if (advancedSettings == null) {
            return;
        }
        advancedSettings.setVisible(showAdvancedSettings.isSelected());
        updateQwenSettingsVisibility();
        updateDeepSeekSettingsVisibility();
        updateOpenAiSettingsVisibility();
        panel.revalidate();
        panel.repaint();
    }

    private void updateOptionsVisibility() {
        if (panel == null) {
            return;
        }
        for (Component component : panel.getComponents()) {
            if (component != enabled) {
                component.setVisible(enabled.isSelected());
            }
        }
        if (enabled.isSelected()) {
            updateAdvancedSettingsVisibility();
        }
        panel.revalidate();
        panel.repaint();
    }

    private void validateConfiguration() throws ConfigurationException {
        if (apiUrl.getText().isBlank() || model.getText().isBlank()) {
            throw new ConfigurationException(bundle().getString("plugin.ai.error.apiUrlAndModelRequired"));
        }
        String value = apiUrl.getText().trim().toLowerCase();
        if (!value.startsWith("https://") && !value.startsWith("http://localhost") && !value.startsWith("http://127.0.0.1")) {
            throw new ConfigurationException(bundle().getString("plugin.ai.error.httpsRequired"));
        }
        if (systemPrompt.getText().isBlank()) {
            throw new ConfigurationException(bundle().getString("plugin.ai.error.systemPromptRequired"));
        }
        validateQwenOptions();
        validateDeepSeekOptions();
        validateOpenAiOptions();
    }

    private void validateOpenAiOptions() throws ConfigurationException {
        if (selectedProvider() != AiProviderType.CHATGPT) {
            return;
        }
        Double topP;
        try {
            topP = requiredOptionalDouble(openAiTopP);
        } catch (ConfigurationException exception) {
            throw new ConfigurationException(bundle().getString("plugin.ai.openai.error.invalidOption"));
        }
        if (topP != null && (topP <= 0D || topP > 1D)) {
            throw new ConfigurationException(bundle().getString("plugin.ai.openai.error.invalidOption"));
        }
    }

    private void validateDeepSeekOptions() throws ConfigurationException {
        if (selectedProvider() != AiProviderType.DEEPSEEK) {
            return;
        }
        Double topP;
        try {
            topP = requiredOptionalDouble(deepSeekTopP);
        } catch (ConfigurationException exception) {
            throw new ConfigurationException(bundle().getString("plugin.ai.deepseek.error.invalidOption"));
        }
        if (topP != null && (topP <= 0D || topP > 1D)) {
            throw new ConfigurationException(bundle().getString("plugin.ai.deepseek.error.invalidOption"));
        }
    }

    private void validateQwenOptions() throws ConfigurationException {
        if (selectedProvider() != AiProviderType.QWEN) {
            return;
        }
        Double topP = requiredOptionalDouble(qwenTopP);
        if (topP != null && (topP <= 0D || topP > 1D)) {
            throw qwenValidationError();
        }
        Integer topK = requiredOptionalInteger(qwenTopK);
        if (topK != null && topK < 0) {
            throw qwenValidationError();
        }
        Double repetitionPenalty = requiredOptionalDouble(qwenRepetitionPenalty);
        if (repetitionPenalty != null && repetitionPenalty <= 0D) {
            throw qwenValidationError();
        }
        Double presencePenalty = requiredOptionalDouble(qwenPresencePenalty);
        if (presencePenalty != null && (presencePenalty < -2D || presencePenalty > 2D)) {
            throw qwenValidationError();
        }
        Long seed = requiredOptionalLong(qwenSeed);
        if (seed != null && (seed < 0L || seed > 2_147_483_647L)) {
            throw qwenValidationError();
        }
        Integer thinkingBudget = requiredOptionalInteger(qwenThinkingBudget);
        if (thinkingBudget != null && thinkingBudget < 0) {
            throw qwenValidationError();
        }
        if (qwenEnableThinking.isSelected() && thinkingBudget != null && selectedOptional(qwenReasoningEffort) != null) {
            throw new ConfigurationException(bundle().getString("plugin.ai.qwen.error.thinkingConflict"));
        }
    }

    private ConfigurationException qwenValidationError() {
        return new ConfigurationException(bundle().getString("plugin.ai.qwen.error.invalidOption"));
    }

    private void saveApiKey() {
        String key = Messages.showPasswordDialog(null, bundle().getString("plugin.ai.apiKeyHint"),
                bundle().getString("plugin.ai.saveApiKeyTitle"), null, null);
        if (key == null || key.isBlank()) {
            return;
        }
        AiProviderType provider = selectedProvider();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            credentialStore.saveApiKey(provider, key.trim());
            ApplicationManager.getApplication().invokeLater(this::refreshCredentialStatus);
        });
    }

    private void clearApiKey() {
        int result = Messages.showYesNoDialog(panel, bundle().getString("plugin.ai.clearApiKeyConfirmation"),
                bundle().getString("plugin.ai.clearApiKeyTitle"), null);
        if (result != Messages.YES) {
            return;
        }
        AiProviderType provider = selectedProvider();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            credentialStore.clearApiKey(provider);
            ApplicationManager.getApplication().invokeLater(this::refreshCredentialStatus);
        });
    }

    private void refreshCredentialStatus() {
        if (credentialStatus == null) {
            return;
        }
        AiProviderType provider = selectedProvider();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            boolean configured = credentialStore.hasCredential(provider);
            ApplicationManager.getApplication().invokeLater(() -> {
                if (credentialStatus != null && selectedProvider() == provider) {
                    credentialStatus.setText(bundle().getString(configured
                            ? "plugin.ai.apiKeyConfigured" : "plugin.ai.apiKeyNotConfigured"));
                }
            });
        });
    }

    private AiProviderType selectedProvider() {
        Object selected = providerType == null ? state.getProviderType() : providerType.getSelectedItem();
        return selected instanceof AiProviderType provider ? provider : AiProviderType.QWEN;
    }

    private Map<AiProviderType, String> draftPromptsWithCurrentEditor() {
        Map<AiProviderType, String> result = normalizePrompts(draftSystemPrompts);
        String current = systemPrompt.getText().trim();
        if (current.isBlank() || current.equals(AiSystemPromptTemplates.forProvider(selectedProvider()))) {
            result.remove(selectedProvider());
        } else {
            result.put(selectedProvider(), current);
        }
        return result;
    }

    private List<String> patternsFromEditor() {
        return Arrays.stream(excludePatterns.getText().split("\\R"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private JLabel addLabeled(String label, JComponent component, GridBagConstraints constraints) {
        constraints.gridy++;
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JLabel labelComponent = new JLabel(label);
        row.add(labelComponent, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        panel.add(row, constraints);
        return labelComponent;
    }

    private record SimpleDocumentListener(Runnable action) implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent event) {
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            action.run();
        }
    }

    private static final class ProviderRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            AiProviderType provider = value instanceof AiProviderType item ? item : AiProviderType.CUSTOM;
            return super.getListCellRendererComponent(list, bundle().getString(provider.displayNameKey()), index, isSelected, cellHasFocus);
        }
    }
}
