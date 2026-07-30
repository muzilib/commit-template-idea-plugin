package com.c301.plugin.config;

import com.c301.plugin.ui.CommitTemplateSettingUI;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 统一的设置入口，同时展示全局默认配置和当前项目覆盖配置。
 */
public class UnifiedCommitTemplateSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll {


    private final GitCommitSettingConfigurable globalConfigurable = new GitCommitSettingConfigurable();
    private final ProjectGitCommitSettingConfigurable projectConfigurable;
    private final CommitMessageRulesConfigurable rulesConfigurable;
    private final PluginPreferencesConfigurable preferencesConfigurable = new PluginPreferencesConfigurable();
    private static volatile boolean selectAiModelTabOnOpen;

    private final AiPreferencesConfigurable aiConfigurable = new AiPreferencesConfigurable();
    private JTabbedPane tabs;

    public UnifiedCommitTemplateSettingsConfigurable(@NotNull Project project) {
        projectConfigurable = new ProjectGitCommitSettingConfigurable(project);
        rulesConfigurable = new CommitMessageRulesConfigurable();
    }

    /**
     * 每个设置页必须可以缩小到其他页首选宽度以下。没有该包装时，JTabbedPane 会继承
     * 最宽子页面的最小尺寸，IDEA 随后会为整个 Configurable 包装水平滚动容器。
     */
    private static JComponent responsiveTab(JComponent content) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(0, 0);
            }
        };
        wrapper.setMinimumSize(new Dimension(0, 0));
        content.setMinimumSize(new Dimension(0, 0));
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "plugins.muzilib.commit.template";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return CommUtil.i18nResourceBundle(null).getString("plugin.setting.displayName");
    }

    /** 请求下一次打开设置时直接定位到 AI 模型标签页。 */
    public static void requestAiModelTabOnOpen() {
        selectAiModelTabOnOpen = true;
    }

    private void selectRequestedTab() {
        if (selectAiModelTabOnOpen && tabs != null) {
            tabs.setSelectedIndex(4);
            selectAiModelTabOnOpen = false;
        }
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (tabs == null) {
            globalConfigurable.createComponent();
            globalConfigurable.reset();
            CommitTemplateSettingUI globalUI = globalConfigurable.getSettingUI();
            tabs = new JTabbedPane();
            tabs.setMinimumSize(new Dimension(0, 0));
            tabs.addTab("", responsiveTab(globalUI.detachSettingsPanel()));
            tabs.addTab("", responsiveTab(projectConfigurable.getSettingsPanel()));
            tabs.addTab("", responsiveTab(rulesConfigurable.createComponent()));
            tabs.addTab("", responsiveTab(preferencesConfigurable.createComponent()));
            tabs.addTab("", responsiveTab(aiConfigurable.createComponent()));
            tabs.addTab("", responsiveTab(globalUI.detachAboutPanel()));
        }
        resetTabTitles();
        selectRequestedTab();
        return tabs;
    }

    @Override
    public boolean isModified() {
        return globalConfigurable.isModified() || projectConfigurable.isModified() || rulesConfigurable.isModified()
                || preferencesConfigurable.isModified() || aiConfigurable.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        globalConfigurable.apply();
        projectConfigurable.apply();
        rulesConfigurable.apply();
        preferencesConfigurable.apply();
        aiConfigurable.apply();
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
        aiConfigurable.reset();
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
        tabs.setTitleAt(4, resourceBundle.getString("plugin.setting.tab.aiModel"));
        tabs.setTitleAt(5, resourceBundle.getString("plugin.setting.label.about"));
    }
}
