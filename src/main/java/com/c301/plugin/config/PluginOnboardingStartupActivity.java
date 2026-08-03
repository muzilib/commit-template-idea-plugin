package com.c301.plugin.config;

import com.c301.plugin.ui.PluginNotifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;

/**
 * 项目打开后展示当前版本的一次性公告。
 */
public final class PluginOnboardingStartupActivity implements ProjectActivity {
    private static final Logger LOG = Logger.getInstance(PluginOnboardingStartupActivity.class);

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
            if (announcement == null) {
                LOG.warn("未找到当前插件版本对应的公告配置");
                return;
            }
            PluginOnboardingState state = PluginOnboardingState.getInstance();
            if (!state.isAnnouncementNeeded(announcement.version())) {
                LOG.info("跳过已展示的版本公告: " + announcement.version());
                return;
            }
            if (PluginNotifications.notifyVersionAnnouncement(project, announcement)) {
                state.markAnnouncementShown(announcement.version());
            }
        }, ModalityState.nonModal(), project.getDisposed());
    }

    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        showAnnouncementIfNeeded(project);
        return Unit.INSTANCE;
    }
}
