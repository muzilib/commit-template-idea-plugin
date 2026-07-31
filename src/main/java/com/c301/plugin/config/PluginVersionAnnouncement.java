package com.c301.plugin.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

    public String version() {
        return version;
    }

    public String messageKeyPrefix() {
        return messageKeyPrefix;
    }

    public static PluginVersionAnnouncement current() {
        Properties properties = new Properties();
        try (InputStream input = PluginVersionAnnouncement.class.getResourceAsStream("/version.properties")) {
            if (input == null) {
                return null;
            }
            properties.load(input);
            return find(properties.getProperty("pluginVersion"));
        } catch (IOException exception) {
            return null;
        }
    }

    private static PluginVersionAnnouncement find(String version) {
        for (PluginVersionAnnouncement announcement : values()) {
            if (announcement.version.equals(version)) {
                return announcement;
            }
        }
        return null;
    }
}
