package com.c301.plugin.config;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.LanguageDomain;
import com.intellij.DynamicBundle;

import java.util.Locale;

/**
 * 独立于提交内容语言，解析插件设置页和弹窗使用的界面语言。
 */
public final class PluginUiLanguageSettings {
    private PluginUiLanguageSettings() {
    }

    public static LanguageDomain resolve(StoreCommitTemplateState state) {
        return state.isSyncUiLanguageWithIde() ? fromLocale(DynamicBundle.getLocale())
                : (state.getUiLanguage() == null ? LanguageDomain.EN_US : state.getUiLanguage());
    }

    public static LanguageDomain fromLocale(Locale locale) {
        String key = locale.getLanguage() + "_" + locale.getCountry();
        return Constant.LANGUAGES.stream()
                .filter(language -> language.getKey().equalsIgnoreCase(key))
                .findFirst()
                .orElseGet(() -> Constant.LANGUAGES.stream()
                        .filter(language -> language.getKey().startsWith(locale.getLanguage() + "_"))
                        .findFirst()
                        .orElse(LanguageDomain.EN_US));
    }
}
