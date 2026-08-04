package com.c301.plugin.config;

import com.c301.plugin.infrastructure.credentials.PasswordSafeAiCredentialStore;
import com.c301.plugin.ui.CommitTemplateSettingUI;
import com.c301.plugin.ui.PluginNotifications;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 统一的设置入口，同时展示全局默认配置和当前项目覆盖配置。
 */
public class UnifiedCommitTemplateSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll {


    private static final int COMMIT_TEMPLATE_TAB_INDEX = 0;
    private static final int COMMIT_RULES_TAB_INDEX = 2;
    private static final int AI_MODEL_TAB_INDEX = 4;
    private static final String SETTINGS_DISPLAY_NAME = "Commit Template Idea Plugin";
    /**
     * Settings 树持有的是扩展点创建的 ConfigurableWrapper，不能用临时实例替代。
     * 按项目暂存一次标签请求，由注册实例在创建或重置时消费。
     */
    private static final Map<Project, Integer> REQUESTED_TAB_INDEXES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private final Project project;
    private final GitCommitSettingConfigurable globalConfigurable = new GitCommitSettingConfigurable();
    private final ProjectGitCommitSettingConfigurable projectConfigurable;
    private final CommitMessageRulesConfigurable rulesConfigurable;
    private final PluginPreferencesConfigurable preferencesConfigurable;
    private final AiPreferencesConfigurable aiConfigurable = new AiPreferencesConfigurable();
    private JTabbedPane tabs;

    public UnifiedCommitTemplateSettingsConfigurable(@NotNull Project project) {
        this.project = project;
        projectConfigurable = new ProjectGitCommitSettingConfigurable(project);
        rulesConfigurable = new CommitMessageRulesConfigurable();
        preferencesConfigurable = new PluginPreferencesConfigurable(this::resetAllConfiguration,
                this::showCurrentVersionAnnouncement);
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

    /**
     * 打开插件的统一设置项，并默认选中提交模板标签页。
     */
    public static void openCommitTemplateSettings(Project project) {
        openSettings(project, COMMIT_TEMPLATE_TAB_INDEX);
    }

    /**
     * 打开插件的统一设置项，并默认选中 AI 模型标签页。
     */
    public static void openAiModelSettings(Project project) {
        openSettings(project, AI_MODEL_TAB_INDEX);
    }

    /**
     * 打开提交规则页，供 AI 校验失败通知提供快捷调整入口。
     */
    public static void openCommitRulesSettings(Project project) {
        openSettings(project, COMMIT_RULES_TAB_INDEX);
    }

    private static void openSettings(Project project, int tabIndex) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }
            REQUESTED_TAB_INDEXES.put(project, tabIndex);
            // IDEA 2023.3 的该重载按 Settings 树节点的显示名称定位，不按 Configurable ID 定位。
            com.intellij.openapi.options.ShowSettingsUtil.getInstance().showSettingsDialog(project, SETTINGS_DISPLAY_NAME);
        });
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "plugins.muzilib.commit.template";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return CommUtil.i18nResourceBundle(null).getString("plugin.setting.displayName");
    }

    private void applyRequestedTab() {
        Integer tabIndex = REQUESTED_TAB_INDEXES.remove(project);
        if (tabs != null && tabIndex != null && tabIndex >= 0 && tabIndex < tabs.getTabCount()) {
            tabs.setSelectedIndex(tabIndex);
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
        applyRequestedTab();
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
        aiConfigurable.refreshLanguage();
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
        applyRequestedTab();
    }

    private void resetAllConfiguration() {
        var bundle = CommUtil.i18nResourceBundle(null);
        int choice = Messages.showYesNoDialog(project, bundle.getString("plugin.preferences.resetAll.confirmation"),
                bundle.getString("plugin.preferences.resetAll.confirmationTitle"),
                bundle.getString("plugin.preferences.resetAll.confirm"), bundle.getString("plugin.preferences.resetAll.cancel"), null);
        if (choice != Messages.YES) {
            return;
        }
        StoreCommitTemplateState.getInstance().loadState(new StoreCommitTemplateState());
        AiPreferencesState.getInstance().resetToDefaults();
        ProjectCommitTemplateOverrideState.getInstance(project).clearOverrides();
        PluginOnboardingState.getInstance().clearAnnouncementHistory();

        // 先在 EDT 恢复所有页面，避免用户在系统钥匙串操作期间点击 Apply 又写回旧值。
        reset();
        showCurrentVersionAnnouncement();

        // Password Safe 可能访问系统钥匙串，只允许在后台清理 API Key。
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                new PasswordSafeAiCredentialStore().clearAllApiKeys());
    }

    private void showCurrentVersionAnnouncement() {
        PluginVersionAnnouncement announcement = PluginVersionAnnouncement.current();
        if (announcement != null) {
            PluginNotifications.showVersionAnnouncementBalloon(project, announcement);
        }
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
