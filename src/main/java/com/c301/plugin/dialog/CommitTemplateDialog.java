package com.c301.plugin.dialog;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.dialog.render.GitCommitLogRender;
import com.c301.plugin.dialog.render.LanguageListCellRendererRender;
import com.c301.plugin.model.ChangeTypeEnum;
import com.c301.plugin.model.CommitMessage;
import com.c301.plugin.model.LanguageDomain;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

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
        setMinimumSize(new Dimension(880, 650));

        buttonOK.addActionListener(e -> handleOKEvent());
        buttonCancel.addActionListener(e -> handleCancelEvent());
        optionLanguage.addActionListener(e -> {
            var language = (LanguageDomain) optionLanguage.getSelectedItem();
            if (language == null) language = OPTINS_LANGUAGE_LIST.get(0);

            handleDisplayLanguageEvent(language.getKey());
        });
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
        handleCancelEvent();
    }

    /**
     * 处理取消事件
     */
    private void handleCancelEvent() {
        //设置语言配置
        var language = (LanguageDomain) optionLanguage.getSelectedItem();
        if (language != null) {
            PropertiesComponent.getInstance().setValue(Constant.STORE_LANGUAGE_KEY, language.getKey());
        }

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

        //读取默认的语言配置，显示窗口
        var languageKey = PropertiesComponent.getInstance().getValue(Constant.STORE_LANGUAGE_KEY, "en_US");
        var languageDomain = OPTINS_LANGUAGE_LIST.stream()
                .filter(item -> item.getKey().equals(languageKey))
                .findFirst()
                .orElse(OPTINS_LANGUAGE_LIST.get(0));
        optionLanguage.setSelectedItem(languageDomain);
        handleDisplayLanguageEvent(languageKey);
        setVisible(true);
    }

    /**
     * 处理显示语言切换
     *
     * @param languageKey 语言标识
     */
    private void handleDisplayLanguageEvent(String languageKey) {
        //获取多语言配置和窗口宽度
        Locale locale = null;
        switch (languageKey) {
            case "zh_CN":
                locale = Locale.SIMPLIFIED_CHINESE;
                setMinimumSize(new Dimension(880, 700));
                break;
            case "zh_TW":
                locale = Locale.TRADITIONAL_CHINESE;
                setMinimumSize(new Dimension(880, 700));
                break;
            case "fr_FR":
                locale = Locale.FRANCE;
                setMinimumSize(new Dimension(1100, 700));
                break;
            case "fr_CA":
                locale = Locale.CANADA_FRENCH;
                setMinimumSize(new Dimension(1100, 700));
                break;
            case "de_DE":
                locale = Locale.GERMANY;
                setMinimumSize(new Dimension(950, 700));
                break;
            case "it_IT":
                locale = Locale.ITALY;
                setMinimumSize(new Dimension(960, 700));
                break;
            case "ja_JP":
                locale = Locale.JAPAN;
                setMinimumSize(new Dimension(880, 700));
                break;
            case "ko_KR":
                locale = Locale.KOREA;
                setMinimumSize(new Dimension(880, 700));
                break;
            default:
                locale = Locale.US;
                setMinimumSize(new Dimension(880, 700));
                break;
        }
        var resourceBundle = ResourceBundle.getBundle("i18n.data", locale);

        //页面显示配置
        setTitle(resourceBundle.getString("plugin.name"));
        buttonOK.setText(resourceBundle.getString("plugin.button.ok"));
        buttonCancel.setText(resourceBundle.getString("plugin.button.cancel"));

        //标题信息
        labelLanguage.setText(resourceBundle.getString("plugin.label.language"));
        labelTypeOfChange.setText(resourceBundle.getString("plugin.label.typeOfChange"));
        labelScopeChang.setText(resourceBundle.getString("plugin.label.scopeOfThisChange"));
        labelShortDescription.setText(resourceBundle.getString("plugin.label.shortDescription"));
        labelLongDescription.setText(resourceBundle.getString("plugin.label.longDescription"));
        labelBreakingChange.setText(resourceBundle.getString("plugin.label.breakingChanges"));
        labelClosedIssues.setText(resourceBundle.getString("plugin.label.closedIssues"));

        //复选框信息
        checkBoxWrapText.setText(resourceBundle.getString("plugin.checkbox.wrapAt72Characters"));
        checkBoxSkipCI.setText(resourceBundle.getString("plugin.checkbox.skipCI"));

        //语言信息
        radioButtonFeat.setText(resourceBundle.getString("plugin.radio.feat"));
        radioButtonFix.setText(resourceBundle.getString("plugin.radio.fix"));
        radioButtonDocs.setText(resourceBundle.getString("plugin.radio.docs"));
        radioButtonStyle.setText(resourceBundle.getString("plugin.radio.style"));
        radioButtonRefactor.setText(resourceBundle.getString("plugin.radio.refactor"));
        radioButtonPerf.setText(resourceBundle.getString("plugin.radio.perf"));
        radioButtonTest.setText(resourceBundle.getString("plugin.radio.test"));
        radioButtonBuild.setText(resourceBundle.getString("plugin.radio.build"));
        radioButtonCi.setText(resourceBundle.getString("plugin.radio.ci"));
        radioButtonChore.setText(resourceBundle.getString("plugin.radio.chore"));
        radioButtonRevert.setText(resourceBundle.getString("plugin.radio.revert"));
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(-1, -1));
        contentPane.setPreferredSize(new Dimension(-1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(9, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelScopeChang = new JLabel();
        labelScopeChang.setText("Scope of this chang");
        panel3.add(labelScopeChang, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        optionScopeChange = new JComboBox();
        optionScopeChange.setEditable(true);
        Font optionScopeChangeFont = UIManager.getFont("Label.font");
        if (optionScopeChangeFont != null) optionScopeChange.setFont(optionScopeChangeFont);
        panel3.add(optionScopeChange, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelShortDescription = new JLabel();
        labelShortDescription.setText("Short description");
        panel3.add(labelShortDescription, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelLongDescription = new JLabel();
        labelLongDescription.setText("Long description");
        panel3.add(labelLongDescription, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        inputLongDescription = new JTextArea();
        Font inputLongDescriptionFont = UIManager.getFont("Label.font");
        if (inputLongDescriptionFont != null) inputLongDescription.setFont(inputLongDescriptionFont);
        scrollPane1.setViewportView(inputLongDescription);
        labelBreakingChange = new JLabel();
        labelBreakingChange.setText("Breaking changes");
        panel3.add(labelBreakingChange, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel3.add(scrollPane2, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        inputBreakingChanges = new JTextArea();
        Font inputBreakingChangesFont = UIManager.getFont("Label.font");
        if (inputBreakingChangesFont != null) inputBreakingChanges.setFont(inputBreakingChangesFont);
        scrollPane2.setViewportView(inputBreakingChanges);
        checkBoxWrapText = new JCheckBox();
        checkBoxWrapText.setActionCommand("Wrap text at 72 characters?");
        checkBoxWrapText.setAutoscrolls(false);
        checkBoxWrapText.setSelected(true);
        checkBoxWrapText.setText("Wrap at 72 characters?");
        panel3.add(checkBoxWrapText, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelClosedIssues = new JLabel();
        labelClosedIssues.setText("Closed issues");
        panel3.add(labelClosedIssues, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        inputClosedIssues = new JTextField();
        Font inputClosedIssuesFont = UIManager.getFont("Label.font");
        if (inputClosedIssuesFont != null) inputClosedIssues.setFont(inputClosedIssuesFont);
        panel3.add(inputClosedIssues, new GridConstraints(7, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        inputShortDescription = new JTextField();
        Font inputShortDescriptionFont = UIManager.getFont("Label.font");
        if (inputShortDescriptionFont != null) inputShortDescription.setFont(inputShortDescriptionFont);
        panel3.add(inputShortDescription, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        checkBoxSkipCI = new JCheckBox();
        checkBoxSkipCI.setSelected(false);
        checkBoxSkipCI.setText("Skip CI?");
        panel3.add(checkBoxSkipCI, new GridConstraints(8, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelTypeOfChange = new JLabel();
        labelTypeOfChange.setText("Type of change");
        panel3.add(labelTypeOfChange, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(11, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        radioButtonFeat = new JRadioButton();
        radioButtonFeat.setActionCommand("feat");
        radioButtonFeat.setSelected(true);
        radioButtonFeat.setText("feat - A new feature");
        panel4.add(radioButtonFeat, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButtonFix = new JRadioButton();
        radioButtonFix.setActionCommand("fix");
        radioButtonFix.setSelected(false);
        radioButtonFix.setText("fix - A bug fix");
        panel4.add(radioButtonFix, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButtonDocs = new JRadioButton();
        radioButtonDocs.setActionCommand("docs");
        radioButtonDocs.setText("docs - Documentation only changes");
        panel4.add(radioButtonDocs, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButtonStyle = new JRadioButton();
        radioButtonStyle.setActionCommand("style");
        radioButtonStyle.setText("style - Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)");
        panel4.add(radioButtonStyle, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButtonRefactor = new JRadioButton();
        radioButtonRefactor.setActionCommand("refactor");
        radioButtonRefactor.setText("refactor - A code change that neither fixes a bug nor adds a feature");
        panel4.add(radioButtonRefactor, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButtonPerf = new JRadioButton();
        radioButtonPerf.setActionCommand("perf");
        radioButtonPerf.setSelected(false);
        radioButtonPerf.setText("perf - A code change that improves performance");
        panel4.add(radioButtonPerf, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButtonTest = new JRadioButton();
        radioButtonTest.setActionCommand("test");
        radioButtonTest.setText("test - Adding missing tests or correcting existing tests");
        panel4.add(radioButtonTest, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButtonBuild = new JRadioButton();
        radioButtonBuild.setActionCommand("build");
        radioButtonBuild.setText("build - Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)");
        panel4.add(radioButtonBuild, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButtonCi = new JRadioButton();
        radioButtonCi.setActionCommand("ci");
        radioButtonCi.setText("ci - Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)");
        panel4.add(radioButtonCi, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButtonChore = new JRadioButton();
        radioButtonChore.setActionCommand("chore");
        radioButtonChore.setText("chore - Other changes that don't modify src or test files");
        panel4.add(radioButtonChore, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButtonRevert = new JRadioButton();
        radioButtonRevert.setActionCommand("revert");
        radioButtonRevert.setText("revert - Reverts a previous commit");
        panel4.add(radioButtonRevert, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelLanguage = new JLabel();
        labelLanguage.setText("Language");
        panel3.add(labelLanguage, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        optionLanguage = new JComboBox();
        panel3.add(optionLanguage, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        typeChangeGroup = new ButtonGroup();
        typeChangeGroup.add(radioButtonFeat);
        typeChangeGroup.add(radioButtonFeat);
        typeChangeGroup.add(radioButtonFix);
        typeChangeGroup.add(radioButtonDocs);
        typeChangeGroup.add(radioButtonStyle);
        typeChangeGroup.add(radioButtonRefactor);
        typeChangeGroup.add(radioButtonPerf);
        typeChangeGroup.add(radioButtonTest);
        typeChangeGroup.add(radioButtonBuild);
        typeChangeGroup.add(radioButtonCi);
        typeChangeGroup.add(radioButtonChore);
        typeChangeGroup.add(radioButtonRevert);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
