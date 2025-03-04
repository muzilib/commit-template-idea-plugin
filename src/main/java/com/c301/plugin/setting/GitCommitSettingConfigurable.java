package com.c301.plugin.setting;

import com.c301.plugin.dialog.CommitTemplateDialog;
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
 * @ClassName com.c301.plugin.setting.GitCommitSettingConfig
 * @Author Chenbing
 * @Date 25/03/04 23:38
 * @Version 1.0
 **/
public class GitCommitSettingConfigurable implements SearchableConfigurable {

    @Override
    public @NotNull @NonNls String getId() {
        return "plugins.muzilib.commit.template";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "设置中心";
    }

    @Override
    public @Nullable JComponent createComponent() {
        var dialog = new CommitTemplateDialog(null);
        return dialog.getManPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

}
