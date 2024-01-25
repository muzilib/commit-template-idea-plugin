package com.c301.plugin.commit;

import com.c301.plugin.localization.PluginBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Damien Arrachequesne Chenbing
 */
public class CommitDialog extends DialogWrapper {

    private final CommitPanel panel;

    CommitDialog(@Nullable Project project, CommitMessage commitMessage) {
        super(project);
        assert project != null;
        panel = new CommitPanel(project, commitMessage);
        setTitle(PluginBundle.get("plugin.commit.panel.title"));
        setOKButtonText(PluginBundle.get("plugin.commit.panel.btn.ok"));
        setCancelButtonText(PluginBundle.get("plugin.commit.panel.btn.cancel"));
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel.getMainPanel();
    }

    CommitMessage getCommitMessage() {
        return panel.getCommitMessage();
    }

}
