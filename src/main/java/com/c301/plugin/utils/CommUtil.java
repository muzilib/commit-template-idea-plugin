package com.c301.plugin.utils;

import com.c301.plugin.constant.Constant;
import com.intellij.ide.util.PropertiesComponent;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 通用工具类
 *
 * @Title CommUtil
 * @ClassName com.c301.plugin.utils.CommUtil
 * @Author Chenbing
 * @Date 25/03/04 17:33
 * @Version 1.0
 **/
public class CommUtil {

    /**
     * 获取国际化资源
     *
     * @param languageKey 语言key
     * @return ResourceBundle
     */
    public static ResourceBundle i18nResourceBundle(String languageKey) {
        if (StrUtil.isBlank(languageKey)) {
            languageKey = PropertiesComponent.getInstance().getValue(Constant.STORE_LANGUAGE_KEY, "en_US");
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

}
