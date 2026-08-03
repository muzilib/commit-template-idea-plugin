package com.c301.plugin.ui;

import com.c301.plugin.config.PluginVersionAnnouncement;
import com.c301.plugin.config.UnifiedCommitTemplateSettingsConfigurable;
import com.c301.plugin.utils.CommUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;

import com.intellij.openapi.diagnostic.Logger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * 统一创建带插件名称和图标的 IDEA 通知，避免不同入口的展示样式不一致。
 */
public final class PluginNotifications {
    private static final Logger LOG = Logger.getInstance(PluginNotifications.class);
    private static final String DEFAULT_NOTIFICATION_GROUP_ID = "commit-template-notify";
    private static final String ANNOUNCEMENT_NOTIFICATION_GROUP_ID = "commit-template-announcement";
    private static final Icon PLUGIN_ICON = scaledIcon(
            IconLoader.getIcon("/META-INF/pluginIcon.svg", PluginNotifications.class), JBUI.scale(32));

    private PluginNotifications() {
    }

    public static void notify(Project project, String content, NotificationType type) {
        NotificationGroup notificationGroup = getNotificationGroup(DEFAULT_NOTIFICATION_GROUP_ID);
        if (notificationGroup == null) {
            return;
        }
        var notification = notificationGroup.createNotification(
                CommUtil.i18nResourceBundle(null).getString("plugin.setting.displayName"), content, type);
        notification.setIcon(PLUGIN_ICON);
        notification.notify(project);
    }

    public static boolean notifyVersionAnnouncement(Project project, PluginVersionAnnouncement announcement) {
        NotificationGroup notificationGroup = getNotificationGroup(ANNOUNCEMENT_NOTIFICATION_GROUP_ID);
        if (notificationGroup == null) {
            return false;
        }
        String title = announcementTitle(announcement);
        String content = announcementContent(announcement);
        var notification = notificationGroup.createNotification(title, content, NotificationType.INFORMATION);
        String prefix = announcement.messageKeyPrefix();
        var bundle = CommUtil.i18nResourceBundle(null);
        notification.addAction(NotificationAction.createSimpleExpiring(bundle.getString(prefix + ".openAiSettings"), () ->
                UnifiedCommitTemplateSettingsConfigurable.openAiModelSettings(project)));
        notification.addAction(NotificationAction.createSimpleExpiring(bundle.getString(prefix + ".openRepository"),
                () -> BrowserUtil.browse("https://github.com/muzilib/commit-template-idea-plugin")));
        notification.setIcon(PLUGIN_ICON);
        notification.notify(project);
        LOG.info("已提交版本公告通知: " + announcement.version());
        return true;
    }

    /**
     * 手动触发时直接投递右下角公告气泡，不等待 Settings 窗口关闭。
     */
    public static boolean showVersionAnnouncementBalloon(Project project, PluginVersionAnnouncement announcement) {
        return notifyVersionAnnouncement(project, announcement);
    }

    private static String announcementTitle(PluginVersionAnnouncement announcement) {
        var bundle = CommUtil.i18nResourceBundle(null);
        return bundle.getString(announcement.messageKeyPrefix() + ".title")
                .replace("{version}", announcement.version());
    }

    private static String announcementContent(PluginVersionAnnouncement announcement) {
        var bundle = CommUtil.i18nResourceBundle(null);
        String prefix = announcement.messageKeyPrefix();
        return "<html>"
                + bundle.getString(prefix + ".greeting") + "<br/><br/>"
                + bundle.getString(prefix + ".feedback") + "<br/><br/>"
                + "<b>" + bundle.getString(prefix + ".tipsTitle") + "</b><br/>"
                + bundle.getString(prefix + ".tips")
                + "</html>";
    }

    private static NotificationGroup getNotificationGroup(String groupId) {
        NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(groupId);
        if (notificationGroup == null) {
            LOG.warn("未注册通知组: " + groupId);
        }
        return notificationGroup;
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
