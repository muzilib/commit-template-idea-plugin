package com.c301.plugin.localization;

import com.intellij.AbstractBundle;
import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.invoke.MethodHandles;
import java.util.ResourceBundle;
import java.util.function.Supplier;

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

    public static Supplier<@Nls String> lazy(@PropertyKey(resourceBundle = "i18n.data") String key, Object... params) {
        return INSTANCE.getLazyMessage(key, params);
    }

    @Override
    protected @NotNull ResourceBundle findBundle(@NotNull @NonNls String pathToBundle, @NotNull ClassLoader loader, ResourceBundle.@NotNull Control control) {
        return ResourceBundle.getBundle(pathToBundle, DynamicBundle.getLocale(), loader, control);
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
