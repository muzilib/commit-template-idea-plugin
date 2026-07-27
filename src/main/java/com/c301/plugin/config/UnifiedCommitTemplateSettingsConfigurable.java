package com.c301.plugin.config;

import com.c301.plugin.ui.CommitTemplateSettingUI;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * A single settings entry that exposes global defaults and the current project's overrides.
 */
public class UnifiedCommitTemplateSettingsConfigurable implements SearchableConfigurable {
    private final GitCommitSettingConfigurable globalConfigurable = new GitCommitSettingConfigurable();
    private final ProjectGitCommitSettingConfigurable projectConfigurable;
    private final CommitMessageRulesConfigurable rulesConfigurable;
    private final PluginPreferencesConfigurable preferencesConfigurable = new PluginPreferencesConfigurable();
    private JTabbedPane tabs;

    public UnifiedCommitTemplateSettingsConfigurable(@NotNull Project project) {
        projectConfigurable = new ProjectGitCommitSettingConfigurable(project);
        rulesConfigurable = new CommitMessageRulesConfigurable();
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "plugins.muzilib.commit.template";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return CommUtil.i18nResourceBundle(null).getString("plugin.setting.displayName");
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (tabs == null) {
            globalConfigurable.createComponent();
            globalConfigurable.reset();
            CommitTemplateSettingUI globalUI = globalConfigurable.getSettingUI();
            tabs = new JTabbedPane();
            tabs.addTab("", globalUI.detachSettingsPanel());
            tabs.addTab("", projectConfigurable.getSettingsPanel());
            tabs.addTab("", rulesConfigurable.createComponent());
            tabs.addTab("", preferencesConfigurable.createComponent());
            tabs.addTab("", globalUI.detachAboutPanel());
        }
        resetTabTitles();
        return tabs;
    }

    @Override
    public boolean isModified() {
        return globalConfigurable.isModified() || projectConfigurable.isModified() || rulesConfigurable.isModified()
                || preferencesConfigurable.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        globalConfigurable.apply();
        projectConfigurable.apply();
        rulesConfigurable.apply();
        preferencesConfigurable.apply();
        globalConfigurable.reset();
        projectConfigurable.reset();
        rulesConfigurable.refreshLanguage();
        preferencesConfigurable.refreshLanguage();
        resetTabTitles();
        tabs.revalidate();
        tabs.repaint();
    }

    @Override
    public void reset() {
        globalConfigurable.reset();
        projectConfigurable.reset();
        rulesConfigurable.reset();
        preferencesConfigurable.reset();
        resetTabTitles();
    }

    private void resetTabTitles() {
        if (tabs == null) {
            return;
        }
        var language = PluginUiLanguageSettings.resolve(StoreCommitTemplateState.getInstance());
        var resourceBundle = CommUtil.i18nResourceBundle(language.getKey());
        tabs.setTitleAt(0, resourceBundle.getString("plugin.setting.tab.commitTemplate"));
        tabs.setTitleAt(1, resourceBundle.getString("plugin.setting.tab.projectOverrides"));
        tabs.setTitleAt(2, resourceBundle.getString("plugin.setting.tab.commitRules"));
        tabs.setTitleAt(3, resourceBundle.getString("plugin.setting.tab.preferences"));
        tabs.setTitleAt(4, resourceBundle.getString("plugin.setting.label.about"));
    }
}
