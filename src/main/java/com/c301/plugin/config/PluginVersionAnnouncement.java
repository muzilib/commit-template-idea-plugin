package com.c301.plugin.config;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 按插件版本维护首次安装与升级提示。发布新版本时在此处新增对应条目和国际化文案。
 */
public enum PluginVersionAnnouncement {
    V1_1_1("1.1.1", "plugin.announcement.v1_0_9"),
    V1_0_9("1.0.9", "plugin.announcement.v1_0_9");

    private static final String BUILD_INFO_RESOURCE = "/version.properties";

    private final String version;
    private final String messageKeyPrefix;

    PluginVersionAnnouncement(String version, String messageKeyPrefix) {
        this.version = version;
        this.messageKeyPrefix = messageKeyPrefix;
    }

    /**
     * 从构建生成的版本资源读取插件版本，避免依赖 IntelliJ Platform 内部 API。
     */
    public static @Nullable PluginVersionAnnouncement current() {
        Properties properties = new Properties();
        try (InputStream input = PluginVersionAnnouncement.class.getResourceAsStream(BUILD_INFO_RESOURCE)) {
            if (input == null) {
                return null;
            }
            properties.load(input);
            return find(properties.getProperty("pluginVersion"));
        } catch (IOException | IllegalArgumentException exception) {
            return null;
        }
    }

    private static @Nullable PluginVersionAnnouncement find(String version) {
        if (version == null || version.isBlank()) {
            return null;
        }
        for (PluginVersionAnnouncement announcement : values()) {
            if (announcement.matches(version)) {
                return announcement;
            }
        }
        return null;
    }

    private boolean matches(String actualVersion) {
        return version.equals(actualVersion)
                || actualVersion.startsWith(version + "-")
                || actualVersion.startsWith(version + "+");
    }

    public String version() {
        return version;
    }

    public String messageKeyPrefix() {
        return messageKeyPrefix;
    }
}
