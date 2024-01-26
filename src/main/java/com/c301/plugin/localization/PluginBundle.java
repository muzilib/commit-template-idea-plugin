package com.c301.plugin.localization;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.PropertyKey;

import java.lang.invoke.MethodHandles;
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
public class PluginBundle extends AbstractBundle {
    public static final PluginBundle INSTANCE = new PluginBundle();

    private PluginBundle() {
        super("i18n.data");
    }

    public static String get(@PropertyKey(resourceBundle = "i18n.data") String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    /**
     * Borrows code from {@code com.intellij.DynamicBundle} to set the parent bundle using reflection.
     */
    private static void setParent(ResourceBundle localeBundle, ResourceBundle base) {
        try {
            var method = ResourceBundle.class.getDeclaredMethod("setParent", ResourceBundle.class);
            method.setAccessible(true);
            MethodHandles.lookup().unreflect(method).bindTo(localeBundle).invoke(base);
        } catch (Throwable ignored) {
        }
    }

}
