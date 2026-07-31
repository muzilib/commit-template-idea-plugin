package com.c301.plugin.config;

import com.c301.plugin.ui.PluginNotifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 项目打开后展示当前版本的一次性公告。
 */
public final class PluginOnboardingStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        PluginVersionAnnouncement announcement = PluginVersionAnnouncement.current();
        if (announcement != null && PluginOnboardingState.getInstance().markAnnouncementNeeded(announcement.version())) {
            PluginNotifications.notifyVersionAnnouncement(project, announcement);
        }
    }
}
