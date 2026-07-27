package com.c301.plugin.config;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.CommitTypeDomain;

import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.model.SettingCacheDomain;
import com.c301.plugin.ui.render.CustomTableCellRenderer;
import com.c301.plugin.ui.render.JBCommitTypeTable;
import com.c301.plugin.ui.render.LanguageListCellRendererRender;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ToolbarDecorator;

import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Project-specific, non-sensitive overrides for commit template behavior.
 * API keys and model credentials deliberately remain global and will be stored in Password Safe.
 */
public class ProjectGitCommitSettingConfigurable implements SearchableConfigurable {
    private final Project project;
    private final ProjectCommitTemplateOverrideState state;
    private final SettingCacheDomain cache = new SettingCacheDomain();

    private JPanel panel;
    private JCheckBox overrideLanguage;
    private JComboBox<LanguageDomain> language;
    private JCheckBox overrideCustomTemplate;
    private JCheckBox customTemplate;
    private JCheckBox overrideCommitTypeList;
    private JPanel commitTypePanel;
    private JBCommitTypeTable commitTypeTable;
    private JComponent commitTypeEditor;
    private JButton insertSystemDefaultsButton;

    public ProjectGitCommitSettingConfigurable(@NotNull Project project) {
        this.project = project;
        this.state = ProjectCommitTemplateOverrideState.getInstance(project);
    }

    private static String text(String key) {
        return CommUtil.i18nResourceBundle(null).getString(key);
    }

    private static void disableHorizontalScrolling(Component component) {
        if (component instanceof JScrollPane scrollPane) {
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                disableHorizontalScrolling(child);
            }
        }
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "plugins.muzilib.commit.template.project";
    }

    @Override
    public @NotNull String getDisplayName() {
        return text("plugin.project.displayName");
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (panel == null) {
            panel = createPanel();
        }
        reset();
        return panel;
    }

    public JComponent getSettingsPanel() {
        return createComponent();
    }

    @Override
    public boolean isModified() {
        if (panel == null) {
            return false;
        }
        return overrideLanguage.isSelected() == (state.getLanguage() == null)
                || (overrideLanguage.isSelected() && !language.getSelectedItem().equals(state.getLanguage()))
                || overrideCustomTemplate.isSelected() == (state.getCustomEnable() == null)
                || (overrideCustomTemplate.isSelected() && customTemplate.isSelected() != state.getCustomEnable())
                || overrideCommitTypeList.isSelected() != Boolean.TRUE.equals(state.getCustomCommitTypeListConfigured())
                || (overrideCommitTypeList.isSelected() && !sameCommitTypes(cache.getCustomCommitTypeList(), state.getCustomCommitTypeList()));
    }

    @Override
    public void apply() throws ConfigurationException {
        state.setLanguage(overrideLanguage.isSelected() ? (LanguageDomain) language.getSelectedItem() : null);
        state.setCustomEnable(overrideCustomTemplate.isSelected() ? customTemplate.isSelected() : null);
        state.setCustomCommitTypeListConfigured(overrideCommitTypeList.isSelected() ? Boolean.TRUE : null);
        state.setCustomCommitTypeList(overrideCommitTypeList.isSelected()
                ? CommUtil.deepCopy(cache.getCustomCommitTypeList())
                : new LinkedList<>());
    }

    @Override
    public void reset() {
        if (panel == null) {
            return;
        }
        EffectiveCommitTemplateSettings effective = CommitTemplateSettingsResolver.getInstance(project).resolve();

        cache.setLanguage(effective.language());
        cache.setEmojiEnable(effective.emojiEnable());
        cache.setEmojiLocation(effective.emojiLocation());
        cache.setCustomEnable(effective.customEnable());
        cache.setCustomCommitTypeList(new LinkedList<>(CommUtil.deepCopy(Boolean.TRUE.equals(state.getCustomCommitTypeListConfigured())
                ? state.getCustomCommitTypeList()
                : effective.customCommitTypeList())));

        overrideLanguage.setSelected(state.getLanguage() != null);
        language.setSelectedItem(state.getLanguage() != null ? state.getLanguage() : effective.language());
        overrideCustomTemplate.setSelected(state.getCustomEnable() != null);
        customTemplate.setSelected(state.getCustomEnable() != null ? state.getCustomEnable() : effective.customEnable());
        overrideCommitTypeList.setSelected(Boolean.TRUE.equals(state.getCustomCommitTypeListConfigured()));
        commitTypeTable.handleRefreshEvent();
        updateEnabledState();
    }

    private JPanel createPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel header = new JPanel(new GridBagLayout());
        GridBagConstraints headerConstraints = new GridBagConstraints();
        headerConstraints.gridx = 0;
        headerConstraints.gridy = 0;
        headerConstraints.weightx = 1;
        headerConstraints.fill = GridBagConstraints.HORIZONTAL;
        headerConstraints.anchor = GridBagConstraints.WEST;
        JTextArea securityHint = new JTextArea(text("plugin.project.securityHint"));
        securityHint.setEditable(false);
        securityHint.setFocusable(false);
        securityHint.setOpaque(false);
        securityHint.setLineWrap(true);
        securityHint.setWrapStyleWord(true);
        securityHint.setColumns(0);
        securityHint.setMinimumSize(new Dimension(0, securityHint.getPreferredSize().height));
        securityHint.setFont(UIManager.getFont("Label.font"));
        securityHint.setForeground(UIManager.getColor("Label.disabledForeground"));
        header.add(securityHint, headerConstraints);
        JButton resetOverrides = new JButton(text("plugin.project.restoreDefaults"));
        resetOverrides.addActionListener(e -> {
            state.clearOverrides();
            reset();
        });
        headerConstraints.gridy++;
        headerConstraints.weightx = 0;
        headerConstraints.fill = GridBagConstraints.NONE;
        headerConstraints.insets = JBUI.insetsTop(6);
        header.add(resetOverrides, headerConstraints);
        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = JBUI.insets(8, 0, 2, 0);

        overrideLanguage = new JCheckBox(text("plugin.project.overrideContentLanguage"));
        content.add(overrideLanguage, constraints);
        constraints.gridy++;
        constraints.insets = JBUI.insets(0, 24, 8, 0);
        language = new JComboBox<>();
        language.setRenderer(new LanguageListCellRendererRender());
        language.setPreferredSize(JBUI.size(240, language.getPreferredSize().height));
        language.setMinimumSize(JBUI.size(0, language.getPreferredSize().height));
        Constant.LANGUAGES.forEach(language::addItem);
        content.add(language, constraints);

        constraints.gridy++;
        constraints.insets = JBUI.insets(8, 0, 2, 0);
        overrideCustomTemplate = new JCheckBox(text("plugin.project.overrideCustomTemplate"));
        content.add(overrideCustomTemplate, constraints);
        constraints.gridy++;
        constraints.insets = JBUI.insets(0, 24, 8, 0);
        customTemplate = new JCheckBox(text("plugin.project.useCustomTypes"));
        content.add(customTemplate, constraints);

        constraints.gridy++;
        constraints.insets = JBUI.insets(8, 0, 2, 0);
        overrideCommitTypeList = new JCheckBox(text("plugin.project.overrideTypeList"));
        content.add(overrideCommitTypeList, constraints);
        constraints.gridy++;
        constraints.insets = JBUI.insets(0, 24, 8, 0);
        commitTypePanel = new JPanel(new BorderLayout());
        commitTypePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        commitTypePanel.setMinimumSize(new Dimension(0, 0));
        commitTypeTable = new JBCommitTypeTable(cache);
        commitTypeTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        commitTypeEditor = ToolbarDecorator.createDecorator(commitTypeTable)
                .setAddAction(button -> commitTypeTable.handlesAddActionEvent())
                .setRemoveAction(button -> commitTypeTable.handlesRemoveActionEvent())
                .setEditAction(button -> commitTypeTable.handlesEditActionEvent())
                .setMoveUpAction(button -> commitTypeTable.handlesMoveUpActionEvent())
                .setMoveDownAction(button -> commitTypeTable.handlesMoveDownActionEvent())
                .createPanel();
        disableHorizontalScrolling(commitTypeEditor);
        var commitTypeEditorPanel = new JPanel(new BorderLayout(0, 4));
        commitTypeEditorPanel.setMinimumSize(new Dimension(0, 0));
        insertSystemDefaultsButton = new JButton(text("plugin.setting.insertSystemDefaults"));
        insertSystemDefaultsButton.addActionListener(e -> {
            LanguageDomain selectedLanguage = (LanguageDomain) language.getSelectedItem();
            int inserted = commitTypeTable.importSystemDefaults(selectedLanguage == null
                    ? CommitTemplateSettingsResolver.getInstance(project).resolve().language() : selectedLanguage);
            var resourceBundle = CommUtil.i18nResourceBundle(null);
            String message = inserted == 0
                    ? resourceBundle.getString("plugin.setting.insertSystemDefaults.none")
                    : resourceBundle.getString("plugin.setting.insertSystemDefaults.result")
                    .replace("{count}", String.valueOf(inserted));
            JOptionPane.showMessageDialog(panel, message, resourceBundle.getString("plugin.setting.dialog.warning"),
                    JOptionPane.INFORMATION_MESSAGE);
        });
        var commitTypeActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        commitTypeActions.add(insertSystemDefaultsButton);
        commitTypeEditorPanel.add(commitTypeActions, BorderLayout.NORTH);
        commitTypeEditorPanel.add(commitTypeEditor, BorderLayout.CENTER);
        commitTypePanel.add(commitTypeEditorPanel, BorderLayout.CENTER);
        content.add(commitTypePanel, constraints);


        overrideLanguage.addActionListener(e -> updateEnabledState());
        overrideCustomTemplate.addActionListener(e -> updateEnabledState());
        overrideCommitTypeList.addActionListener(e -> updateEnabledState());
        customTemplate.addActionListener(e -> updateEnabledState());
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private void updateEnabledState() {
        language.setEnabled(overrideLanguage.isSelected());
        customTemplate.setEnabled(overrideCustomTemplate.isSelected());
        // A project type-list override is independently usable and automatically becomes the
        // project's custom type source. It must not depend on the separate template checkbox.
        boolean typeListEnabled = overrideCommitTypeList.isSelected();
        commitTypePanel.setEnabled(typeListEnabled);
        commitTypeTable.setEnabled(typeListEnabled);
        commitTypeEditor.setEnabled(typeListEnabled);
        insertSystemDefaultsButton.setEnabled(typeListEnabled);
        cache.setEmojiEnable(CommitTemplateSettingsResolver.getInstance(project).resolve().emojiEnable());
        commitTypeTable.handleRefreshEvent();
    }

    private boolean sameCommitTypes(LinkedList<CommitTypeDomain> left, LinkedList<CommitTypeDomain> right) {
        if (left == null || right == null) {
            return left == right;
        }
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            if (!left.get(index).hashString().equals(right.get(index).hashString())) {
                return false;
            }
        }
        return true;
    }

}
