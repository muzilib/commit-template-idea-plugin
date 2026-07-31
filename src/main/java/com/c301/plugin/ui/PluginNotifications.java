package com.c301.plugin.ui;

import com.c301.plugin.config.PluginVersionAnnouncement;
import com.c301.plugin.config.UnifiedCommitTemplateSettingsConfigurable;
import com.c301.plugin.utils.CommUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * 统一创建带插件名称和图标的 IDEA 通知，避免不同入口的展示样式不一致。
 */
public final class PluginNotifications {
    private static final Icon PLUGIN_ICON = scaledIcon(
            IconLoader.getIcon("/META-INF/pluginIcon.svg", PluginNotifications.class), JBUI.scale(32));

    private PluginNotifications() {
    }

    public static void notify(Project project, String content, NotificationType type) {
        var notification = NotificationGroupManager.getInstance().getNotificationGroup("commit-template-notify")
                .createNotification(CommUtil.i18nResourceBundle(null).getString("plugin.setting.displayName"), content, type);
        notification.setIcon(PLUGIN_ICON);
        notification.notify(project);
    }

    @SuppressWarnings("deprecation")
    public static void notifyVersionAnnouncement(Project project, PluginVersionAnnouncement announcement) {
        var bundle = CommUtil.i18nResourceBundle(null);
        String prefix = announcement.messageKeyPrefix();
        String title = bundle.getString(prefix + ".title").replace("{version}", announcement.version());
        String content = "<html>"
                + bundle.getString(prefix + ".greeting") + "<br/><br/>"
                + bundle.getString(prefix + ".feedback") + "<br/><br/>"
                + "<b>" + bundle.getString(prefix + ".tipsTitle") + "</b><br/>"
                + bundle.getString(prefix + ".tips") + "<br/><br/>"
                + "<a href='ai-settings'>" + bundle.getString(prefix + ".openAiSettings") + "</a>"
                + "&nbsp;&nbsp;<a href='repository'>" + bundle.getString(prefix + ".openRepository") + "</a>"
                + "</html>";
        var notification = NotificationGroupManager.getInstance().getNotificationGroup("commit-template-notify")
                .createNotification(title, content, NotificationType.INFORMATION);
        notification.setListener((clicked, event) -> {
            if ("ai-settings".equals(event.getDescription())) {
                UnifiedCommitTemplateSettingsConfigurable.requestAiModelTabOnOpen();
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "plugins.muzilib.commit.template");
            } else if ("repository".equals(event.getDescription())) {
                BrowserUtil.browse("https://github.com/muzilib/commit-template-idea-plugin");
            }
        });
        notification.setIcon(PLUGIN_ICON);
        notification.notify(project);
    }

    /**
     * 插件图标源文件为 480px 画布，通知中必须使用固定尺寸，避免撑大 IDEA 原生通知气泡。
     */
    private static Icon scaledIcon(Icon source, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component component, Graphics graphics, int x, int y) {
                Graphics2D scaledGraphics = (Graphics2D) graphics.create();
                try {
                    scaledGraphics.translate(x, y);
                    scaledGraphics.scale((double) size / source.getIconWidth(), (double) size / source.getIconHeight());
                    source.paintIcon(component, scaledGraphics, 0, 0);
                } finally {
                    scaledGraphics.dispose();
                }
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }
}
