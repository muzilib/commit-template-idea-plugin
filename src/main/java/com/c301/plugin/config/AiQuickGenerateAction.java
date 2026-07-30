package com.c301.plugin.config;

import com.c301.plugin.platform.vcs.AiIncludedChangesCollector;

import com.c301.plugin.ui.AiDirectStreamingGenerator;
import com.c301.plugin.ui.AiGenerationDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.Refreshable;
import com.intellij.vcs.commit.CommitProjectPanelAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Commit 工具栏的 AI 快捷入口。只有全局 AI 开关开启时才显示，且不会执行 Git 提交。
 */
public final class AiQuickGenerateAction extends AnAction implements DumbAware {
    private static CommitMessageI findCommitMessage(AnActionEvent event) {
        Refreshable refreshable = Refreshable.PANEL_KEY.getData(event.getDataContext());
        if (refreshable instanceof CommitMessageI message) {
            return message;
        }
        return VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(event.getDataContext());
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        boolean enabled = AiPreferencesState.getInstance().isEnabled();
        event.getPresentation().setVisible(enabled);
        event.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        CommitMessageI commitMessage = findCommitMessage(event);
        if (project == null && commitMessage instanceof CommitProjectPanelAdapter adapter) {
            project = adapter.getProject();
        }
        if (project == null || commitMessage == null) {
            return;
        }
        Project targetProject = project;
        CommitMessageI targetCommitMessage = commitMessage;
        ApplicationManager.getApplication().invokeLater(() -> {
            var changes = AiIncludedChangesCollector.collectMetadata(event, AiPreferencesState.getInstance());
            if (changes.includedMetadata().isEmpty()) {
                AiGenerationDialog.notifyNoEligibleChanges(targetProject);
                return;
            }
            if (AiPreferencesState.getInstance().isCheckDiffBeforeSending()) {
                new AiGenerationDialog(targetProject, targetCommitMessage, changes).setVisible(true);
            } else {
                new AiDirectStreamingGenerator(targetProject, targetCommitMessage, changes).generate();
            }
        }, ModalityState.current());
    }
}
