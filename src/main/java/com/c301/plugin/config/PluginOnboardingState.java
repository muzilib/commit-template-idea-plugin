package com.c301.plugin.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 记录已展示的版本公告，避免同一版本在每次启动时重复打扰用户。
 */
@Data
@NoArgsConstructor
@State(name = "CommitTemplateOnboarding", storages = @Storage("commit-template-onboarding.xml"))
public final class PluginOnboardingState implements PersistentStateComponent<PluginOnboardingState> {
    private String announcedVersion;

    public static PluginOnboardingState getInstance() {
        return ApplicationManager.getApplication().getService(PluginOnboardingState.class);
    }

    @Override
    public @Nullable PluginOnboardingState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PluginOnboardingState state) {
        announcedVersion = state.announcedVersion;
    }

    public boolean isAnnouncementNeeded(String version) {
        return version != null && !version.isBlank() && !version.equals(announcedVersion);
    }

    /**
     * 仅在公告已提交给 IDEA 通知系统后记录版本，避免 UI 尚未就绪时丢失首次提示。
     */
    public void markAnnouncementShown(String version) {
        if (version != null && !version.isBlank()) {
            announcedVersion = version;
        }
    }

    public void clearAnnouncementHistory() {
        announcedVersion = null;
    }
}
