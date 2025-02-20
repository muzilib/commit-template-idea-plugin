package com.c301.plugin.commit;

import com.c301.plugin.dialog.CommitTemplateDialog;
import com.c301.plugin.model.CommitMessage;
import org.jetbrains.annotations.NotNull;

/**
 * 创建提交行动事件
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
            return refreshable;
        }

        return VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(dataContext);
    }

}
