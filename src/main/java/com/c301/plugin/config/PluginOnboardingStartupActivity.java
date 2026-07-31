package com.c301.plugin.config;

import com.c301.plugin.ui.PluginNotifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 项目打开后展示当前版本的一次性公告。
 */
public final class PluginOnboardingStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        showAnnouncementIfNeeded(project);
    }

    /**
     * 仅在没有模态窗口时创建通知。重置按钮位于 Settings 模态窗口中，若继承当前模态状态，
     * 通知气泡会被设置窗口遮挡或直接折叠到通知中心。
     */
    public static void showAnnouncementIfNeeded(@NotNull Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }
            PluginVersionAnnouncement announcement = PluginVersionAnnouncement.current();
            PluginOnboardingState state = PluginOnboardingState.getInstance();
            if (announcement != null && state.isAnnouncementNeeded(announcement.version())) {
                PluginNotifications.notifyVersionAnnouncement(project, announcement);
                state.markAnnouncementShown(announcement.version());
            }
        }, ModalityState.nonModal(), project.getDisposed());
    }
}
