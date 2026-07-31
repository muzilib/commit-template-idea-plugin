package com.c301.plugin.config;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginDescriptor;
import org.jetbrains.annotations.Nullable;

/**
 * 按插件版本维护首次安装与升级提示。发布新版本时在此处新增对应条目和国际化文案。
 */
public enum PluginVersionAnnouncement {
    V1_0_9("1.0.9", "plugin.announcement.v1_0_9");

    private final String version;
    private final String messageKeyPrefix;

    PluginVersionAnnouncement(String version, String messageKeyPrefix) {
        this.version = version;
        this.messageKeyPrefix = messageKeyPrefix;
    }

    /**
     * 从 IDEA 已加载插件的描述符读取版本，避免公告依赖未被打包进插件 JAR 的额外资源。
     */
    public static @Nullable PluginVersionAnnouncement current() {
        PluginDescriptor descriptor = PluginManager.getPluginByClass(PluginVersionAnnouncement.class);
        return descriptor == null ? null : find(descriptor.getVersion());
    }

    private static @Nullable PluginVersionAnnouncement find(String version) {
        for (PluginVersionAnnouncement announcement : values()) {
            if (announcement.version.equals(version)) {
                return announcement;
            }
        }
        return null;
    }

    public String version() {
        return version;
    }

    public String messageKeyPrefix() {
        return messageKeyPrefix;
    }
}
