package com.c301.plugin.action;

import com.c301.plugin.dialog.CommitTemplateDialog;
import com.c301.plugin.model.CommitMessage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.Refreshable;
import org.jetbrains.annotations.NotNull;

/**
 * 提交事件
 *
 * @author Damien Arrachequesne Chenbing
 */
public class CreateCommitAction extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        var commitMessage = new CommitMessage();
        var commitMessageI = getCommitMessagePanel(actionEvent);
        if (commitMessageI instanceof CheckinProjectPanel) {
            var content = ((CheckinProjectPanel) commitMessageI).getCommitMessage();
            commitMessage = CommitMessage.parseRawMessage(content);
        }

        var dialog = new CommitTemplateDialog(commitMessageI);
        dialog.init(actionEvent.getProject(), commitMessage);
    }

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

}
