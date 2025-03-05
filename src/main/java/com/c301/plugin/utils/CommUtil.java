package com.c301.plugin.utils;

import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.setting.StoreCommitTemplateState;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.c301.plugin.constant.Constant.OPTINS_LANGUAGE_LIST;

/**
 * 通用工具类
 *
 * @Title CommUtil
 * @ClassName com.c301.plugin.utils.CommUtil
 * @Author Chenbing
 * @Date 25 /03/04 17:33
 * @Version 1.0
 */
public class CommUtil {

    /**
     * 获取国际化资源
     *
     * @param languageKey 语言key
     * @return ResourceBundle resource bundle
     */
    public static ResourceBundle i18nResourceBundle(String languageKey) {
        if (StrUtil.isBlank(languageKey)) {
            languageKey = StoreCommitTemplateState.getInstance().storeConfig.language;
            if (StrUtil.isBlank(languageKey))
                languageKey = StoreCommitTemplateState.getInstance().defaultValue.language;
        }

        var locale = switch (languageKey) {
            case "zh_CN" -> Locale.SIMPLIFIED_CHINESE;
            case "zh_TW" -> Locale.TRADITIONAL_CHINESE;
            case "fr_FR" -> Locale.FRANCE;
            case "fr_CA" -> Locale.CANADA_FRENCH;
            case "de_DE" -> Locale.GERMANY;
            case "it_IT" -> Locale.ITALY;
            case "ja_JP" -> Locale.JAPAN;
            case "ko_KR" -> Locale.KOREA;
            default -> Locale.US;
        };
        return ResourceBundle.getBundle("i18n.data", locale);
    }

    /**
     * 获取语言实体
     *
     * @param languageKey the language key
     * @return language domain
     */
    public static LanguageDomain convertLanguageDomain(String languageKey) {
        return OPTINS_LANGUAGE_LIST.stream()
                .filter(item -> item.getKey().equals(languageKey))
                .findFirst()
                .orElse(OPTINS_LANGUAGE_LIST.get(0));
    }

    /**
     * 获取选择器的语言实体
     *
     * @param optionLanguage the option language
     * @return language domain
     */
    public static LanguageDomain convertLanguageDomain(JComboBox<LanguageDomain> optionLanguage) {
        var language = (LanguageDomain) optionLanguage.getSelectedItem();
        if (language == null) language = OPTINS_LANGUAGE_LIST.get(0);
        return language;
    }

}
