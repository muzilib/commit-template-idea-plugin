package com.c301.plugin.commit;

import com.c301.plugin.localization.PluginBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;

/**
 * @author Damien Arrachequesne Chenbing
 */
public class CommitDialog extends DialogWrapper {

    private final CommitPanel panel;

    CommitDialog(Project project, CommitMessageOld commitMessage) {
        super(project);
        assert project != null;
        panel = new CommitPanel(project, commitMessage);
        setTitle(PluginBundle.get("plugin.commit.panel.title"));
        setOKButtonText(PluginBundle.get("plugin.commit.panel.btn.ok"));
        setCancelButtonText(PluginBundle.get("plugin.commit.panel.btn.cancel"));
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        return panel.getMainPanel();
    }

    CommitMessageOld getCommitMessage() {
        return panel.getCommitMessage();
    }

}
