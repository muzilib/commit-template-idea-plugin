package com.c301.plugin.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @Title CommitDialog
 * @ClassName com.c301.plugin.commit.CommitDialog
 * @Author Chenbing
 * @Date 2024/01/24 10:48
 * @Version 1.0
 **/
public class CommitDialog extends DialogWrapper {

    private final CommitPanel panel;

    CommitDialog(@Nullable Project project, CommitMessage commitMessage) {
        super(project);
        assert project != null;
        panel = new CommitPanel(project, commitMessage);
        setTitle("Commit By _Chenbing");
        setOKButtonText("OK Button");
        init();
    }


    @Override
    protected JComponent createCenterPanel() {
        return panel.getMainPanel();
    }

    CommitMessage getCommitMessage() {
        return panel.getCommitMessage();
    }

}
