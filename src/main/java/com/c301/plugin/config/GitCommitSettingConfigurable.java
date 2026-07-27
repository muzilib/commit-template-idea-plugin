package com.c301.plugin.config;

import com.c301.plugin.model.SettingCacheDomain;
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
    private final SettingCacheDomain cache = new SettingCacheDomain();
    private CommitTemplateSettingUI commitSettingUI = null;

    @Override
    public @NotNull @NonNls String getId() {
        return "plugins.muzilib.commit.template";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        var resourceBundle = CommUtil.i18nResourceBundle(null);
        return resourceBundle.getString("plugin.setting.displayName");
    }

    @Override
    public @Nullable JComponent createComponent() {
        cache.setLanguage(store.getLanguage());
        cache.setEmojiEnable(store.isEmojiEnable());
        cache.setEmojiLocation(store.getEmojiLocation());
        cache.setCustomEnable(store.isCustomEnable());
        cache.setCustomCommitTypeList(CommUtil.deepCopy(store.getCustomCommitTypeList()));

        if (commitSettingUI == null) commitSettingUI = new CommitTemplateSettingUI(cache);
        return commitSettingUI.getMainPanel();
    }

    public CommitTemplateSettingUI getSettingUI() {
        return commitSettingUI;
    }

    @Override
    public boolean isModified() {
        if (!cache.getLanguage().equals(store.getLanguage())) return true;
        if (cache.isCustomEnable() != store.isCustomEnable()) return true;
        if (cache.getCustomCommitTypeList().size() != store.getCustomCommitTypeList().size()) return true;

        //比对内容文本是否编辑
        var modified = false;
        for (int i = 0; i < cache.getCustomCommitTypeList().size(); i++) {
            var cacheCommitType = cache.getCustomCommitTypeList().get(i);
            var storeCommitType = store.getCustomCommitTypeList().get(i);
            if (!cacheCommitType.hashString().equals(storeCommitType.hashString())) {
                modified = true;
                break;
            }
        }
        return modified;
    }

    @Override
    public void reset() {
        var cache = new SettingCacheDomain();
        cache.setLanguage(store.getLanguage());
        cache.setEmojiEnable(store.isEmojiEnable());
        cache.setCustomEnable(store.isCustomEnable());
        cache.setEmojiLocation(store.getEmojiLocation());
        cache.setCustomCommitTypeList(CommUtil.deepCopy(store.getCustomCommitTypeList()));
        commitSettingUI.handleResetEvent(cache);
    }

    @Override
    public void apply() throws ConfigurationException {
        store.setLanguage(cache.getLanguage());
        store.setCustomEnable(cache.isCustomEnable());
        store.setCustomCommitTypeList(CommUtil.deepCopy(cache.getCustomCommitTypeList()));
    }

}
