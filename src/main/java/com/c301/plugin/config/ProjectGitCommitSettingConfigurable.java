package com.c301.plugin.config;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitmojiLocationDomain;
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
import com.intellij.ui.components.JBLabel;
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
    private JCheckBox overrideGitmoji;
    private JCheckBox gitmoji;
    private JCheckBox overrideGitmojiLocation;
    private JRadioButton location1;
    private JRadioButton location2;
    private JRadioButton location3;

    public ProjectGitCommitSettingConfigurable(@NotNull Project project) {
        this.project = project;
        this.state = ProjectCommitTemplateOverrideState.getInstance(project);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "plugins.muzilib.commit.template.project";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Commit Template (Project)";
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
                || (overrideCommitTypeList.isSelected() && !sameCommitTypes(cache.getCustomCommitTypeList(), state.getCustomCommitTypeList()))
                || overrideGitmoji.isSelected() == (state.getEmojiEnable() == null)
                || (overrideGitmoji.isSelected() && gitmoji.isSelected() != state.getEmojiEnable())
                || overrideGitmojiLocation.isSelected() == (state.getEmojiLocation() == null)
                || (overrideGitmojiLocation.isSelected() && !selectedLocation().equals(state.getEmojiLocation()));
    }

    @Override
    public void apply() throws ConfigurationException {
        state.setLanguage(overrideLanguage.isSelected() ? (LanguageDomain) language.getSelectedItem() : null);
        state.setCustomEnable(overrideCustomTemplate.isSelected() ? customTemplate.isSelected() : null);
        state.setCustomCommitTypeListConfigured(overrideCommitTypeList.isSelected() ? Boolean.TRUE : null);
        state.setCustomCommitTypeList(overrideCommitTypeList.isSelected()
                ? CommUtil.deepCopy(cache.getCustomCommitTypeList())
                : new LinkedList<>());
        state.setEmojiEnable(overrideGitmoji.isSelected() ? gitmoji.isSelected() : null);
        state.setEmojiLocation(overrideGitmojiLocation.isSelected() ? selectedLocation() : null);
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
        overrideGitmoji.setSelected(state.getEmojiEnable() != null);
        gitmoji.setSelected(state.getEmojiEnable() != null ? state.getEmojiEnable() : effective.emojiEnable());
        overrideGitmojiLocation.setSelected(state.getEmojiLocation() != null);
        selectLocation(state.getEmojiLocation() != null ? state.getEmojiLocation() : effective.emojiLocation());
        commitTypeTable.handleRefreshEvent();
        updateEnabledState();
    }

    private JPanel createPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.add(new JBLabel("Project overrides are stored in .idea/commit-template.xml. Never store API keys here."), BorderLayout.CENTER);
        JButton resetOverrides = new JButton("Restore global defaults");
        resetOverrides.addActionListener(e -> {
            state.clearOverrides();
            reset();
        });
        header.add(resetOverrides, BorderLayout.EAST);
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

        overrideLanguage = new JCheckBox("Override display language");
        content.add(overrideLanguage, constraints);
        constraints.gridy++;
        constraints.insets = JBUI.insets(0, 24, 8, 0);
        language = new JComboBox<>();
        language.setRenderer(new LanguageListCellRendererRender());
        Constant.LANGUAGES.forEach(language::addItem);
        content.add(language, constraints);

        constraints.gridy++;
        constraints.insets = JBUI.insets(8, 0, 2, 0);
        overrideCustomTemplate = new JCheckBox("Override custom commit template setting");
        content.add(overrideCustomTemplate, constraints);
        constraints.gridy++;
        constraints.insets = JBUI.insets(0, 24, 8, 0);
        customTemplate = new JCheckBox("Use custom commit types");
        content.add(customTemplate, constraints);

        constraints.gridy++;
        constraints.insets = JBUI.insets(8, 0, 2, 0);
        overrideCommitTypeList = new JCheckBox("Override project commit type list");
        content.add(overrideCommitTypeList, constraints);
        constraints.gridy++;
        constraints.insets = JBUI.insets(0, 24, 8, 0);
        commitTypePanel = new JPanel(new BorderLayout());
        commitTypePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        commitTypeTable = new JBCommitTypeTable(cache);
        commitTypeTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        commitTypeEditor = ToolbarDecorator.createDecorator(commitTypeTable)
                .setAddAction(button -> commitTypeTable.handlesAddActionEvent())
                .setRemoveAction(button -> commitTypeTable.handlesRemoveActionEvent())
                .setEditAction(button -> commitTypeTable.handlesEditActionEvent())
                .setMoveUpAction(button -> commitTypeTable.handlesMoveUpActionEvent())
                .setMoveDownAction(button -> commitTypeTable.handlesMoveDownActionEvent())
                .createPanel();
        commitTypePanel.add(commitTypeEditor, BorderLayout.CENTER);
        content.add(commitTypePanel, constraints);

        constraints.gridy++;
        constraints.insets = JBUI.insets(8, 0, 2, 0);
        overrideGitmoji = new JCheckBox("Override Gitmoji setting");
        content.add(overrideGitmoji, constraints);
        constraints.gridy++;
        constraints.insets = JBUI.insets(0, 24, 8, 0);
        gitmoji = new JCheckBox("Use Gitmoji");
        content.add(gitmoji, constraints);

        constraints.gridy++;
        constraints.insets = JBUI.insets(8, 0, 2, 0);
        overrideGitmojiLocation = new JCheckBox("Override Gitmoji location");
        content.add(overrideGitmojiLocation, constraints);
        constraints.gridy++;
        constraints.insets = JBUI.insets(0, 24, 2, 0);
        location1 = new JRadioButton("Location 1");
        content.add(location1, constraints);
        constraints.gridy++;
        location2 = new JRadioButton("Location 2");
        content.add(location2, constraints);
        constraints.gridy++;
        constraints.insets = JBUI.insets(0, 24, 8, 0);
        location3 = new JRadioButton("Location 3");
        content.add(location3, constraints);
        ButtonGroup locations = new ButtonGroup();
        locations.add(location1);
        locations.add(location2);
        locations.add(location3);

        overrideLanguage.addActionListener(e -> updateEnabledState());
        overrideCustomTemplate.addActionListener(e -> updateEnabledState());
        overrideCommitTypeList.addActionListener(e -> updateEnabledState());
        customTemplate.addActionListener(e -> updateEnabledState());
        overrideGitmoji.addActionListener(e -> updateEnabledState());
        overrideGitmojiLocation.addActionListener(e -> updateEnabledState());
        gitmoji.addActionListener(e -> {
            cache.setEmojiEnable(gitmoji.isSelected());
            commitTypeTable.handleRefreshEvent();
        });
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private void updateEnabledState() {
        language.setEnabled(overrideLanguage.isSelected());
        customTemplate.setEnabled(overrideCustomTemplate.isSelected());
        boolean typeListEnabled = overrideCommitTypeList.isSelected()
                && (overrideCustomTemplate.isSelected() ? customTemplate.isSelected()
                : CommitTemplateSettingsResolver.getInstance(project).resolve().customEnable());
        commitTypePanel.setEnabled(typeListEnabled);
        commitTypeTable.setEnabled(typeListEnabled);
        commitTypeEditor.setEnabled(typeListEnabled);
        gitmoji.setEnabled(overrideGitmoji.isSelected());
        cache.setEmojiEnable(overrideGitmoji.isSelected() ? gitmoji.isSelected()
                : CommitTemplateSettingsResolver.getInstance(project).resolve().emojiEnable());
        commitTypeTable.handleRefreshEvent();
        boolean locationEnabled = overrideGitmojiLocation.isSelected();
        location1.setEnabled(locationEnabled);
        location2.setEnabled(locationEnabled);
        location3.setEnabled(locationEnabled);
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

    private GitmojiLocationDomain selectedLocation() {
        if (location2.isSelected()) {
            return GitmojiLocationDomain.LOCATION2;
        }
        if (location3.isSelected()) {
            return GitmojiLocationDomain.LOCATION3;
        }
        return GitmojiLocationDomain.LOCATION1;
    }

    private void selectLocation(GitmojiLocationDomain location) {
        if (GitmojiLocationDomain.LOCATION2.equals(location)) {
            location2.setSelected(true);
        } else if (GitmojiLocationDomain.LOCATION3.equals(location)) {
            location3.setSelected(true);
        } else {
            location1.setSelected(true);
        }
    }
}
