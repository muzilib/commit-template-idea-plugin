package com.c301.plugin.dialog;

import com.c301.plugin.dialog.render.GitCommitLogRender;
import com.c301.plugin.dialog.render.LanguageListCellRendererRender;
import com.c301.plugin.model.ChangeTypeEnum;
import com.c301.plugin.model.CommitMessage;
import com.c301.plugin.model.LanguageDomain;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.c301.plugin.constant.Constant.COMMIT_FIRST_LINE_FORMAT;
import static com.c301.plugin.constant.Constant.OPTINS_LANGUAGE_LIST;

/**
 * 提交模板对话框
 *
 * @Title CommitTemplateDialog
 * @ClassName com.c301.plugin.dialog.CommitTemplateDialog
 * @Author Chenbing
 * @Date 25/02/19 11:13
 * @Version 1.0
 **/
public class CommitTemplateDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> optionScopeChange;
    private JCheckBox checkBoxWrapText;
    private JCheckBox checkBoxSkipCI;
    private JTextField inputClosedIssues;
    private JLabel labelTypeOfChange;
    private JRadioButton radioButtonFeat;
    private JRadioButton radioButtonFix;
    private JRadioButton radioButtonDocs;
    private JRadioButton radioButtonStyle;
    private JRadioButton radioButtonRefactor;
    private JRadioButton radioButtonPerf;
    private JRadioButton radioButtonTest;
    private JRadioButton radioButtonBuild;
    private JRadioButton radioButtonCi;
    private JRadioButton radioButtonChore;
    private JRadioButton radioButtonRevert;
    private JLabel labelLanguage;
    private JComboBox<LanguageDomain> optionLanguage;
    private JLabel labelScopeChang;
    private JLabel labelShortDescription;
    private JLabel labelLongDescription;
    private JLabel labelBreakingChange;
    private JLabel labelClosedIssues;
    private JTextArea inputLongDescription;
    private JTextArea inputBreakingChanges;
    private JTextField inputShortDescription;
    private ButtonGroup typeChangeGroup;

    private final CommitMessageI commitMessageI;

    /**
     * 创建弹窗信息
     */
    public CommitTemplateDialog(CommitMessageI commitMessageI) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        //设置显示窗口大小
        this.commitMessageI = commitMessageI;
        pack();
        setMinimumSize(new Dimension(700, 895));

        buttonOK.addActionListener(e -> handleOKEvent());
        buttonCancel.addActionListener(e -> handleCancelEvent());
        optionLanguage.addActionListener(e -> handleLanguageChange());
        optionLanguage.setRenderer(new LanguageListCellRendererRender());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                handleCancelEvent();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> handleCancelEvent(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * 处理确定事件
     */
    private void handleOKEvent() {
        //获取选中的对象
        var changeTypeEnum = ChangeTypeEnum.FEAT;
        var buttonElements = typeChangeGroup.getElements();
        while (buttonElements.hasMoreElements()) {
            var button = buttonElements.nextElement();

            if (button.isSelected()) {
                changeTypeEnum = ChangeTypeEnum.valueOf(button.getActionCommand().toUpperCase());
                break;
            }
        }

        //获取用户提交的数据
        var commitMessage = new CommitMessage();
        commitMessage.setChangeType(changeTypeEnum);
        commitMessage.setChangeScope((String) optionScopeChange.getSelectedItem());
        commitMessage.setShortDescription(inputShortDescription.getText().trim());
        commitMessage.setLongDescription(inputLongDescription.getText().trim());
        commitMessage.setBreakingChanges(inputBreakingChanges.getText().trim());
        commitMessage.setClosedIssues(inputClosedIssues.getText().trim());
        commitMessage.setWrapText(checkBoxWrapText.isSelected());
        commitMessage.setSkipCI(checkBoxSkipCI.isSelected());

        //设置信息到提交面板中
        commitMessageI.setCommitMessage(commitMessage.toRwaString());
        dispose();
    }

    /**
     * 处理取消事件
     */
    private void handleCancelEvent() {
        // add your code here if necessary
        dispose();
    }

    /**
     * 初始化弹窗配置
     */
    public void init(Project project, CommitMessage commitMessage) {
        var result = GitCommitLogRender.handleCommitHistory(project);
        if (result.isSuccess()) {
            // 添加默认值
            optionScopeChange.addItem("");

            //读取提交记录并获取变更范围
            var scopes = new HashSet<String>();
            for (String log : result.getLogs()) {
                var matcher = COMMIT_FIRST_LINE_FORMAT.matcher(log);
                if (matcher.find()) scopes.add(matcher.group(3));
            }
            scopes.forEach(optionScopeChange::addItem);
        }
        OPTINS_LANGUAGE_LIST.forEach(optionLanguage::addItem);

        //设置窗口打开位置为屏幕中心
        setLocationRelativeTo(null);
        var parentWindow = WindowManager.getInstance().getFrame(project);
        if (parentWindow != null) setLocationRelativeTo(parentWindow);

        //回显数据
        if (commitMessage.getChangeType() != null) {
            var changeTypeCode = commitMessage.getChangeType().getCode();
            var buttonElements = typeChangeGroup.getElements();

            while (buttonElements.hasMoreElements()) {
                var button = buttonElements.nextElement();

                if (button.getActionCommand().equalsIgnoreCase(changeTypeCode)) {
                    button.setSelected(true);
                    break;
                }
            }
        }
        optionScopeChange.setSelectedItem(commitMessage.getChangeScope());
        inputShortDescription.setText(commitMessage.getShortDescription());
        inputLongDescription.setText(commitMessage.getLongDescription());
        inputClosedIssues.setText(commitMessage.getClosedIssues());
        inputBreakingChanges.setText(commitMessage.getBreakingChanges());
        checkBoxSkipCI.setSelected(commitMessage.isSkipCI());
        checkBoxWrapText.setSelected(commitMessage.isWrapText());

        //显示窗口
        handleDisplayLanguageEvent("en_US");
        setVisible(true);
    }

    /**
     * 处理显示语言变化
     */
    public void handleLanguageChange() {
        var language = (LanguageDomain) optionLanguage.getSelectedItem();

        if (language != null) handleDisplayLanguageEvent(language.getKey());
        else handleDisplayLanguageEvent("en_US");
    }

    private void handleDisplayLanguageEvent(String languageKey) {
        Locale locale;
        switch (languageKey) {
            case "en_US":
                locale = Locale.US;
                break;
            case "zh_CN":
                locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case "zh_TW":
                locale = Locale.TRADITIONAL_CHINESE;
                break;
            default:
                locale = Locale.US;
                break;
        }

        //获取多语言配置
        var resourceBundle = ResourceBundle.getBundle("i18n.data", locale);

        //页面显示配置
        setTitle(resourceBundle.getString("plugin.commit.panel.title"));
        buttonOK.setText(resourceBundle.getString("plugin.commit.panel.btn.ok"));
        buttonCancel.setText(resourceBundle.getString("plugin.commit.panel.btn.cancel"));

        //标题信息
        labelTypeOfChange.setText(resourceBundle.getString("plugin.commit.typeofchange"));
        labelScopeChang.setText(resourceBundle.getString("plugin.commit.scopeofchange"));
        labelShortDescription.setText(resourceBundle.getString("plugin.commit.shortdescription"));
        labelLongDescription.setText(resourceBundle.getString("plugin.commit.longdescription"));
        labelBreakingChange.setText(resourceBundle.getString("plugin.commit.breakingchanges"));
        checkBoxWrapText.setText(resourceBundle.getString("plugin.commit.wrapat72"));
        checkBoxSkipCI.setText(resourceBundle.getString("plugin.commit.skipci"));
        labelClosedIssues.setText(resourceBundle.getString("plugin.commit.closedissues"));
        labelClosedIssues.setToolTipText(resourceBundle.getString("plugin.commit.closedissueshover"));

        //语言信息
        labelTypeOfChange.setText(resourceBundle.getString("plugin.commit.typeofchange"));
    }

}
