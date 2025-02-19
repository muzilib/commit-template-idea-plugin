package com.c301.plugin.commit;

import com.c301.plugin.localization.PluginBundle;
import com.c301.plugin.utils.StrUtil;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

import static com.c301.plugin.constant.Constant.COMMIT_GITMOJI_FORMAT;

/**
 * 提交面板
 *
 * @author Damien Arrachequesne Chenbing
 */
public class CommitPanel {

    private JPanel mainPanel;
    private JComboBox<String> gitmojiOption;
    private JTextField shortDescription;
    private JTextArea longDescription;
    private JTextArea breakingChanges;
    private JTextField closedIssues;
    private JCheckBox wrapTextCheckBox;
    private JCheckBox skipCICheckBox;
    private JRadioButton featRadioButton;
    private JRadioButton fixRadioButton;
    private JRadioButton docsRadioButton;
    private JRadioButton styleRadioButton;
    private JRadioButton refactorRadioButton;
    private JRadioButton perfRadioButton;
    private JRadioButton testRadioButton;
    private JRadioButton buildRadioButton;
    private JRadioButton ciRadioButton;
    private JRadioButton choreRadioButton;
    private JRadioButton revertRadioButton;
    private ButtonGroup changeTypeGroup;
    private JLabel typeOfChangeLabel;
    private JLabel gitmojiOptionLabel;
    private JLabel scopeOfThisChangeLabel;
    private JLabel shortDescriptionLabel;
    private JLabel longDescriptionLabel;
    private JLabel breakingChangesLabel;
    private JLabel closedIssuesLabel;
    private JComboBox<String> changeScope;

    CommitPanel(Project project, CommitMessage commitMessage) {
        var workingDirectory = new File(Objects.requireNonNull(project.getBasePath()));
        var result = new GitLogQuery(workingDirectory).execute();
        if (result.isSuccess()) {
            changeScope.addItem(""); // no value by default
            result.getScopes().forEach(changeScope::addItem);
        }

        //加载gitmoji
        var selectGitmojiOption = "";
        gitmojiOption.addItem(""); // no value by default
        for (Map<String, String> map : GitmojiType.getDataList()) {
            var data = map.get("name");
            data = PluginBundle.get("plugin.gitmoji." + data);
            data = map.get("icon") + " " + data;
            gitmojiOption.addItem(data);

            //重新设置选中对象
            if (map.get("code").equalsIgnoreCase(commitMessage.getGitmojiCode())) {
                selectGitmojiOption = data;
            }
        }
        if (StrUtil.isBlank(selectGitmojiOption)) {
            selectGitmojiOption = commitMessage.getGitmojiCode();
        }
        gitmojiOption.setSelectedItem(selectGitmojiOption);

        if (commitMessage != null) {
            restoreValuesFromParsedCommitMessage(commitMessage);
        }

        //选项信息
        var textFormat = "%s - %s";
        featRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.feat"), PluginBundle.get("plugin.commit.feat.desc")));
        fixRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.fix"), PluginBundle.get("plugin.commit.fix.desc")));
        docsRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.docs"), PluginBundle.get("plugin.commit.docs.desc")));
        styleRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.style"), PluginBundle.get("plugin.commit.style.desc")));
        refactorRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.refactor"), PluginBundle.get("plugin.commit.refactor.desc")));
        perfRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.perf"), PluginBundle.get("plugin.commit.perf.desc")));
        testRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.test"), PluginBundle.get("plugin.commit.test.desc")));
        buildRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.build"), PluginBundle.get("plugin.commit.build.desc")));
        ciRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.ci"), PluginBundle.get("plugin.commit.ci.desc")));
        choreRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.chore"), PluginBundle.get("plugin.commit.chore.desc")));
        revertRadioButton.setText(String.format(textFormat, PluginBundle.get("plugin.commit.revert"), PluginBundle.get("plugin.commit.revert.desc")));

        //标题信息
        typeOfChangeLabel.setText(PluginBundle.get("plugin.commit.typeofchange"));
        scopeOfThisChangeLabel.setText(PluginBundle.get("plugin.commit.scopeofchange"));
        shortDescriptionLabel.setText(PluginBundle.get("plugin.commit.shortdescription"));
        longDescriptionLabel.setText(PluginBundle.get("plugin.commit.longdescription"));
        breakingChangesLabel.setText(PluginBundle.get("plugin.commit.breakingchanges"));
        wrapTextCheckBox.setText(PluginBundle.get("plugin.commit.wrapat72"));
        skipCICheckBox.setText(PluginBundle.get("plugin.commit.skipci"));
        closedIssuesLabel.setText(PluginBundle.get("plugin.commit.closedissues"));
        closedIssuesLabel.setToolTipText(PluginBundle.get("plugin.commit.closedissueshover"));
        closedIssues.setToolTipText(PluginBundle.get("plugin.commit.closedissueshover"));
        gitmojiOptionLabel.setText(PluginBundle.get("plugin.commit.gitmoji"));
        gitmojiOptionLabel.setToolTipText(PluginBundle.get("plugin.commit.gitmojihover"));
        gitmojiOption.setToolTipText(PluginBundle.get("plugin.commit.gitmojihover"));
    }

    JPanel getMainPanel() {
        return mainPanel;
    }

    CommitMessage getCommitMessage() {
        var gitmoji = "";
        if (gitmojiOption.getSelectedIndex() > 0) {
            gitmoji = GitmojiType.getDataList().get(gitmojiOption.getSelectedIndex() - 1).get("code");
        } else {
            gitmoji = Objects.requireNonNull(gitmojiOption.getSelectedItem()).toString();
            var matcher = COMMIT_GITMOJI_FORMAT.matcher(gitmoji);
            if (matcher.find()) {
                gitmoji = gitmoji.trim();
            }
        }

        return new CommitMessage(
                getSelectedChangeType(),
                (String) changeScope.getSelectedItem(),
                shortDescription.getText().trim(),
                longDescription.getText().trim(),
                breakingChanges.getText().trim(),
                closedIssues.getText().trim(),
                wrapTextCheckBox.isSelected(),
                skipCICheckBox.isSelected(),
                gitmoji
        );
    }

    private ChangeType getSelectedChangeType() {
        for (Enumeration<AbstractButton> buttons = changeTypeGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return ChangeType.valueOf(button.getActionCommand().toUpperCase());
            }
        }
        return null;
    }

    private void restoreValuesFromParsedCommitMessage(CommitMessage commitMessage) {
        if (commitMessage.getChangeType() != null) {
            for (Enumeration<AbstractButton> buttons = changeTypeGroup.getElements(); buttons.hasMoreElements(); ) {
                AbstractButton button = buttons.nextElement();

                if (button.getActionCommand().equalsIgnoreCase(commitMessage.getChangeType().label())) {
                    button.setSelected(true);
                }
            }
        }
        changeScope.setSelectedItem(commitMessage.getChangeScope());
        shortDescription.setText(commitMessage.getShortDescription());
        longDescription.setText(commitMessage.getLongDescription());
        breakingChanges.setText(commitMessage.getBreakingChanges());
        closedIssues.setText(commitMessage.getClosedIssues());
        skipCICheckBox.setSelected(commitMessage.isSkipCI());
    }

}
