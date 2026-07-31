package com.c301.plugin.config;

import com.c301.plugin.model.GitCommitDomain;
import com.c301.plugin.ui.CommitTemplateDialog;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.Refreshable;
import com.intellij.vcs.commit.CommitProjectPanelAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * 提交事件
 *
 * @author Damien Arrachequesne Chenbing
 */
public class CreateCommitAction extends AnAction implements DumbAware {

    /**
     * 获取CommitMessageI
     *
     * @param event AnActionEvent
     * @return CommitMessageI
     */
    private static CommitMessageI getCommitMessagePanel(AnActionEvent event) {
        if (event == null) return null;
        var dataContext = event.getDataContext();

        var refreshable = Refreshable.PANEL_KEY.getData(dataContext);
        if (refreshable instanceof CommitMessageI) {
            return (CommitMessageI) refreshable;
        }

        return VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(dataContext);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        CommUtil.handleInitGitmojiEvent();

        // 设置按钮文本和描述
        var resourceBundle = CommUtil.i18nResourceBundle(null);
        var templatePresentation = getTemplatePresentation();
        templatePresentation.setText(resourceBundle.getString("action.plugin_commit_button.text"));
        templatePresentation.setDescription(resourceBundle.getString("action.plugin_commit_button.description"));

        // 通过快捷键打开窗口
        if (actionEvent.getInputEvent() instanceof KeyEvent && actionEvent.getPlace().equals(ActionPlaces.KEYBOARD_SHORTCUT)) {
            try {
                var checkinProjectAction = ActionManager.getInstance().getAction("CheckinProject");
                ActionManager.getInstance().tryToExecute(
                        checkinProjectAction,
                        actionEvent.getInputEvent(),
                        null,
                        ActionPlaces.UNKNOWN,
                        true
                );
            } catch (Exception ignored) {
            }

            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    var action = ActionManager.getInstance().getAction("plugin_commit_button");
                    ActionManager.getInstance().tryToExecute(
                            action,
                            actionEvent.getInputEvent(),
                            null,
                            ActionPlaces.UNKNOWN,
                            true
                    );
                } catch (Exception ignored) {
                }
            }, ModalityState.current());
            return;
        }

        //正常打开窗口
        var gitCommit = new GitCommitDomain();
        var commitMessageI = getCommitMessagePanel(actionEvent);
        if (commitMessageI instanceof CheckinProjectPanel) {
            var content = ((CheckinProjectPanel) commitMessageI).getCommitMessage();
            gitCommit = GitCommitDomain.parseRawMessage(content);
        }

        // 获取 Project，优先从 actionEvent 获取，如果为 null 则尝试从 commitMessageI 获取
        Project project = actionEvent.getProject();
        if (project == null && commitMessageI instanceof CommitProjectPanelAdapter) {
            project = ((CommitProjectPanelAdapter) commitMessageI).getProject();
        }

        // 等当前 Action 上下文结束后再打开模态窗口，避免 IDEA 2023.3 在嵌套事件循环中重复安装 ActionContext。
        Project targetProject = project;
        GitCommitDomain targetGitCommit = gitCommit;
        SwingUtilities.invokeLater(() -> {
            var dialog = new CommitTemplateDialog(commitMessageI, targetProject, actionEvent);
            dialog.handleUIInit();
            dialog.resetUIFrom(targetGitCommit);
            dialog.setVisible(true);
        });
    }

}
