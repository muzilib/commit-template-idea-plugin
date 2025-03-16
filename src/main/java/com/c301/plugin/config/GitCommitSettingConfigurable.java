package com.c301.plugin.config;

import com.c301.plugin.ui.CommitTemplateSettingUI;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Git模板配置页面
 *
 * @Title GitCommitSettingConfig
 * @ClassName com.c301.plugin.config.GitCommitSettingConfig
 * @Author Chenbing
 * @Date 25/03/04 23:38
 * @Version 1.0
 **/
public class GitCommitSettingConfigurable implements SearchableConfigurable {
    private final StoreCommitTemplateState store = StoreCommitTemplateState.getInstance();
    private CommitTemplateSettingUI commitSettingUI = null;

    @Override
    public @NotNull @NonNls String getId() {
        return "plugins.muzilib.commit.template";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        var resourceBundle = CommUtil.i18nResourceBundle(null);
        return resourceBundle.getString("plugin.setting.label.displayName");
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (commitSettingUI == null) commitSettingUI = new CommitTemplateSettingUI(store);
        return commitSettingUI.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void apply() throws ConfigurationException {

    }

}
