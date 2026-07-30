package com.c301.plugin.config;

import com.c301.plugin.domain.ai.AiDataTransferConsent;
import com.c301.plugin.domain.ai.AiProviderType;
import com.c301.plugin.domain.ai.QwenGenerationOptions;
import com.c301.plugin.infrastructure.ai.AiSystemPromptTemplates;
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
    private JLabel providerHint;
    private JSpinner temperature;
    private JSpinner maxTokens;
    private JTextArea systemPrompt;
    private JTextArea excludePatterns;
    private JLabel credentialStatus;
    private JButton saveKey;
    private JButton clearKey;
    private JButton restorePrompt;
    private JPanel qwenSettings;
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
    private AiProviderType displayedProvider;
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

    private static void addAdvancedLabeled(JPanel panel, String label, JComponent component, GridBagConstraints constraints) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(new JLabel(label), BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        panel.add(row, constraints);
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
            panel.add(new JLabel(bundle.getString("plugin.ai.protocol")), constraints);

            providerType = new JComboBox<>(AiProviderType.values());
            providerType.setRenderer(new ProviderRenderer());
            addLabeled(bundle.getString("plugin.ai.providerType"), providerType, constraints);

            apiUrl = new JTextField();
            addLabeled(bundle.getString("plugin.ai.apiUrl"), apiUrl, constraints);
            providerHint = new JLabel();
            providerHint.setBorder(JBUI.Borders.emptyLeft(4));
            constraints.gridy++;
            panel.add(providerHint, constraints);

            model = new JTextField();
            addLabeled(bundle.getString("plugin.ai.model"), model, constraints);

            JPanel credentials = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            credentialStatus = new JLabel();
            saveKey = new JButton(bundle.getString("plugin.ai.saveApiKey"));
            clearKey = new JButton(bundle.getString("plugin.ai.clearApiKey"));
            credentials.add(credentialStatus);
            credentials.add(saveKey);
            credentials.add(clearKey);
            constraints.gridy++;
            panel.add(credentials, constraints);

            constraints.gridy++;
            showAdvancedSettings = new JCheckBox(bundle.getString("plugin.ai.moreSettings"));
            panel.add(showAdvancedSettings, constraints);
            constraints.gridy++;
            advancedSettings = createAdvancedSettings(bundle);
            panel.add(advancedSettings, constraints);

            constraints.gridy++;
            panel.add(new JLabel(bundle.getString("plugin.ai.excludePatterns")), constraints);
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
            providerType.addActionListener(event -> switchProvider());
            saveKey.addActionListener(event -> saveApiKey());
            clearKey.addActionListener(event -> clearApiKey());
            restorePrompt.addActionListener(event -> restoreDefaultPrompt());
            qwenEnableThinking.addActionListener(event -> updateQwenDependentOptions());
            qwenEnableSearch.addActionListener(event -> updateQwenDependentOptions());
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
        draftSystemPrompts = normalizePrompts(state.getCustomSystemPrompts());
        providerType.setSelectedItem(state.getProviderType());
        model.setText(state.getModel());
        temperature.setValue(state.getTemperature());
        maxTokens.setValue(state.getMaxTokens());
        resetQwenOptions(state.getQwenGenerationOptions());
        excludePatterns.setText(String.join("\n", state.getExcludePatterns()));
        switchProvider();
        // 重置时恢复用户保存的地址，不能被供应商预设的推荐地址覆盖。
        apiUrl.setText(state.getApiUrl());
        refreshCredentialStatus();
        updateAdvancedSettingsVisibility();
        updateOptionsVisibility();
    }

    private void switchProvider() {
        if (systemPrompt == null) {
            return;
        }
        savePromptFor(displayedProvider);
        AiProviderType provider = selectedProvider();
        displayedProvider = provider;
        if (provider.usesPresetApiUrl()) {
            apiUrl.setText(provider.apiUrl());
            providerHint.setText(bundle().getString("plugin.ai.providerApiKeyHint"));
        } else {
            if (apiUrl.getText().equals(AiProviderType.QWEN.apiUrl()) || apiUrl.getText().equals(AiProviderType.CHATGPT.apiUrl())
                    || apiUrl.getText().equals(AiProviderType.DEEPSEEK.apiUrl())) {
                apiUrl.setText("");
            }
            providerHint.setText(bundle().getString("plugin.ai.customProviderHint"));
        }
        apiUrl.setEditable(true);
        model.putClientProperty("JTextField.placeholderText", bundle().getString(provider.modelPlaceholderKey()));
        systemPrompt.setText(promptFor(provider));
        updateQwenSettingsVisibility();
        refreshCredentialStatus();
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
        addAdvancedLabeled(content, bundle.getString("plugin.ai.temperature"), temperature, constraints);
        constraints.gridy++;
        maxTokens = new JSpinner(new SpinnerNumberModel(1024, 1, 16384, 1));
        addAdvancedLabeled(content, bundle.getString("plugin.ai.maxTokens"), maxTokens, constraints);
        constraints.gridy++;
        qwenSettings = createQwenSettings(bundle);
        content.add(qwenSettings, constraints);
        constraints.gridy++;
        content.add(new JLabel(bundle.getString("plugin.ai.systemPrompt")), constraints);
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
        content.setBorder(BorderFactory.createTitledBorder(bundle.getString("plugin.ai.qwen.settings")));
        GridBagConstraints constraints = constraints(0);
        constraints.insets = JBUI.insetsBottom(4);
        qwenIncludeUsage = new JCheckBox(bundle.getString("plugin.ai.qwen.includeUsage"));
        content.add(qwenIncludeUsage, constraints);
        constraints.gridy++;
        content.add(new JLabel(bundle.getString("plugin.ai.qwen.sampling")), constraints);
        constraints.gridy++;
        qwenTopP = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.topP"), qwenTopP, constraints);
        constraints.gridy++;
        qwenTopK = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.topK"), qwenTopK, constraints);
        constraints.gridy++;
        qwenRepetitionPenalty = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.repetitionPenalty"), qwenRepetitionPenalty, constraints);
        constraints.gridy++;
        qwenPresencePenalty = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.presencePenalty"), qwenPresencePenalty, constraints);
        constraints.gridy++;
        qwenSeed = optionalField(bundle, "plugin.ai.qwen.randomOptional");
        addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.seed"), qwenSeed, constraints);
        constraints.gridy++;
        content.add(new JLabel(bundle.getString("plugin.ai.qwen.thinking")), constraints);
        constraints.gridy++;
        qwenEnableThinking = new JCheckBox(bundle.getString("plugin.ai.qwen.enableThinking"));
        content.add(qwenEnableThinking, constraints);
        constraints.gridy++;
        qwenThinkingBudget = optionalField(bundle, "plugin.ai.qwen.defaultOptional");
        addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.thinkingBudget"), qwenThinkingBudget, constraints);
        constraints.gridy++;
        qwenReasoningEffort = new JComboBox<>(new String[]{"", "low", "medium", "high", "xhigh", "max"});
        addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.reasoningEffort"), qwenReasoningEffort, constraints);
        constraints.gridy++;
        content.add(new JLabel(bundle.getString("plugin.ai.qwen.search")), constraints);
        constraints.gridy++;
        qwenEnableSearch = new JCheckBox(bundle.getString("plugin.ai.qwen.enableSearch"));
        content.add(qwenEnableSearch, constraints);
        constraints.gridy++;
        qwenForceSearch = new JCheckBox(bundle.getString("plugin.ai.qwen.forceSearch"));
        content.add(qwenForceSearch, constraints);
        constraints.gridy++;
        qwenSearchStrategy = new JComboBox<>(new String[]{"turbo", "max", "agent", "agent_max"});
        addAdvancedLabeled(content, bundle.getString("plugin.ai.qwen.searchStrategy"), qwenSearchStrategy, constraints);
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

    private void updateAdvancedSettingsVisibility() {
        if (advancedSettings == null) {
            return;
        }
        advancedSettings.setVisible(showAdvancedSettings.isSelected());
        updateQwenSettingsVisibility();
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
        if (key != null && !key.isBlank()) {
            credentialStore.saveApiKey(selectedProvider(), key.trim());
        }
        refreshCredentialStatus();
    }

    private void clearApiKey() {
        int result = Messages.showYesNoDialog(panel, bundle().getString("plugin.ai.clearApiKeyConfirmation"),
                bundle().getString("plugin.ai.clearApiKeyTitle"), null);
        if (result == Messages.YES) {
            credentialStore.clearApiKey(selectedProvider());
        }
        refreshCredentialStatus();
    }

    private void refreshCredentialStatus() {
        if (credentialStatus != null) {
            boolean configured = credentialStore.hasCredential(selectedProvider());
            credentialStatus.setText(bundle().getString(configured ? "plugin.ai.apiKeyConfigured" : "plugin.ai.apiKeyNotConfigured"));
        }
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

    private void addLabeled(String label, JComponent component, GridBagConstraints constraints) {
        constraints.gridy++;
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(new JLabel(label), BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        panel.add(row, constraints);
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
            String key = switch (value instanceof AiProviderType provider ? provider : AiProviderType.CUSTOM) {
                case QWEN -> "plugin.ai.provider.qwen";
                case CHATGPT -> "plugin.ai.provider.chatgpt";
                case DEEPSEEK -> "plugin.ai.provider.deepseek";
                case CUSTOM -> "plugin.ai.provider.custom";
            };
            return super.getListCellRendererComponent(list, bundle().getString(key), index, isSelected, cellHasFocus);
        }
    }
}
