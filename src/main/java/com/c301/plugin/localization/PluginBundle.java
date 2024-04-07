package com.c301.plugin.localization;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 多语言配置信息
 *
 * @Title PluginBundle
 * @ClassName com.c301.plugin.localization.PluginBundle
 * @Author Chenbing
 * @Date 2024/01/25 09:53
 * @Version 1.0
 **/
public class PluginBundle {

    private final static PluginId INTELLIJ_ZH_PLUGIN = PluginId.getId("com.intellij.zh");

    public static Locale getUserLocale() {
        var locale = Locale.ENGLISH;

        //判断用户是否安装并启用了官方的中文插件
        if (PluginManagerCore.getPlugin(INTELLIJ_ZH_PLUGIN) != null) {
            locale = Locale.CHINA;
        }
        return locale;
    }

    /**
     * 获取多语言信息
     *
     * @param key 键
     * @return 值
     */
    public static String get(String key) {
        var locale = getUserLocale();
        var resourceBundle = ResourceBundle.getBundle("i18n.data", locale);
        return resourceBundle.getString(key);
    }

}
