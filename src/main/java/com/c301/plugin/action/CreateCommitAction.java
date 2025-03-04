package com.c301.plugin.action;

import com.c301.plugin.dialog.CommitTemplateDialog;
import com.c301.plugin.model.CommitMessage;
import com.c301.plugin.utils.CommUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.Refreshable;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * 提交事件
 *
 * @author Damien Arrachequesne Chenbing
 */
public class CreateCommitAction extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        // 设置按钮文本和描述
        var resourceBundle = CommUtil.i18nResourceBundle(null);
        var templatePresentation = getTemplatePresentation();
        templatePresentation.setText(resourceBundle.getString("action.plugin_commit_button.text"));
        templatePresentation.setDescription(resourceBundle.getString("action.plugin_commit_button.description"));

        // 通过快捷键打开窗口
        if (actionEvent.getInputEvent() instanceof KeyEvent && actionEvent.getPlace().equals(ActionPlaces.KEYBOARD_SHORTCUT)) {
            ActionManager.getInstance().getAction("CheckinProject").actionPerformed(actionEvent);

            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    var dataContext = DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(2000);
                    var vcsActionEvent = AnActionEvent.createFromAnAction(this, actionEvent.getInputEvent(), ActionPlaces.UNKNOWN, Objects.requireNonNull(dataContext));
                    ActionManager.getInstance().getAction("plugin_commit_button").actionPerformed(vcsActionEvent);
                } catch (Exception ignored) {
                }
            }, ModalityState.current());
            return;
        }

        //正常打开窗口
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
