package com.c301.plugin.commit;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.Refreshable;
import org.jetbrains.annotations.NotNull;

/**
 * 创建提交行动事件
 *
 * @author Damien Arrachequesne Chenbing
 */
public class CreateCommitAction extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        synchronized (CreateCommitAction.class) {
            var commitPanel = getCommitPanel(actionEvent);
            if (commitPanel == null) return;

            var commitMessage = parseExistingCommitMessage(commitPanel);
            //var dialog = new CommitDialog(actionEvent.getProject(), commitMessage);
            var dialog = new CommitDialog2();
            dialog.pack();
            dialog.setVisible(true);
        }
    }

    private static CommitMessageI getCommitPanel(AnActionEvent e) {
        if (e == null) {
            return null;
        }
        var data = Refreshable.PANEL_KEY.getData(e.getDataContext());
        if (data instanceof CommitMessageI) {
            return (CommitMessageI) data;
        }
        return VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.getDataContext());
    }

    private CommitMessage parseExistingCommitMessage(CommitMessageI commitPanel) {
        if (commitPanel instanceof CheckinProjectPanel) {
            var commitMessageString = ((CheckinProjectPanel) commitPanel).getCommitMessage();
            return CommitMessage.parse(commitMessageString);
        }
        return null;
    }

}
