package com.c301.plugin.ui;

import com.c301.plugin.config.StoreCommitTemplateState;
import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitCommitDomain;
import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.model.WindowsConfigDomain;
import com.c301.plugin.ui.render.LanguageListCellRendererRender;
import com.c301.plugin.utils.CommUtil;
import com.c301.plugin.utils.StrUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

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
    private JRadioButton radioButton1;
    private JRadioButton radioButton2;
    private JRadioButton radioButton3;
    private JRadioButton radioButton4;
    private JRadioButton radioButton5;
    private JRadioButton radioButton6;
    private JRadioButton radioButton7;
    private JRadioButton radioButton8;
    private JRadioButton radioButton9;
    private JRadioButton radioButton10;
    private JRadioButton radioButton11;
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

    private final StoreCommitTemplateState store = StoreCommitTemplateState.getInstance();
    private final CommitMessageI commitMessageI;

    /**
     * 创建弹窗信息
     */
    public CommitTemplateDialog(CommitMessageI commitMessageI) {
        $$$setupUI$$$();
        setModal(true);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        this.commitMessageI = commitMessageI;

        //设置显示窗口大小
        pack();
        setMinimumSize(new Dimension(880, 650));

        buttonOK.addActionListener(e -> handleOKEvent());
        buttonCancel.addActionListener(e -> handleCancelEvent());
        optionLanguage.setRenderer(new LanguageListCellRendererRender());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                handleCancelEvent();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> handleCancelEvent(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * 处理确定事件
     */
    private void handleOKEvent() {
        if (commitMessageI != null) {
            //处理提交类型
            CommitTypeDomain commitType = null;
            var buttonElements = typeChangeGroup.getElements();
            while (buttonElements.hasMoreElements()) {
                var button = buttonElements.nextElement();
                if (!button.isSelected()) continue;

                commitType = CommUtil.parseCommitType(button.getActionCommand());
                break;
            }

            //处理变更范围
            var changeScopeValue = "";
            if (optionScopeChange.getSelectedItem() != null) {
                changeScopeValue = optionScopeChange.getSelectedItem().toString();
            }

            //处理关闭问题
            var closedIssuesValue = new LinkedList<Integer>();
            if (StrUtil.isNotBlank(inputClosedIssues.getText())) {
                try {
                    var arrays = inputClosedIssues.getText().trim().replaceAll("，", ",").split(",");
                    for (var item : arrays) {
                        item = item.trim();
                        if (!StrUtil.isNumeric(item)) item = item.replace("#", "");
                        closedIssuesValue.add(Integer.parseInt(item));
                    }
                } catch (Exception ignored) {
                }
            }

            var gitCommit = new GitCommitDomain() {{
                setShortDescription(inputShortDescription.getText());
                setLongDescription(inputLongDescription.getText());
                setBreakingChanges(inputBreakingChanges.getText());
                setWrapText(checkBoxWrapText.isSelected());
                setSkipCI(checkBoxSkipCI.isSelected());
            }};
            gitCommit.setCommitType(commitType);
            gitCommit.setChangeScope(changeScopeValue);
            gitCommit.setClosedIssues(closedIssuesValue);
            commitMessageI.setCommitMessage(gitCommit.toStringMessage());
        }

        handleCancelEvent();
    }

    /**
     * 处理取消事件
     */
    private void handleCancelEvent() {
        var language = CommUtil.convertLanguageDomain(optionLanguage);
        store.setLanguage(language);
        dispose();
    }

    /**
     * 初始化弹窗配置
     */
    public void handleUIInit() {
        //窗口大小发生变化监听事件
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                super.componentMoved(e);
                var windows = store.getCommitWindowConfig();
                if (windows == null) windows = new WindowsConfigDomain();

                windows.setWindowX(getX());
                windows.setWindowY(getY());
                store.setCommitWindowConfig(windows);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                var windows = store.getCommitWindowConfig();
                if (windows == null) windows = new WindowsConfigDomain();

                windows.setWindowWidth(getWidth());
                windows.setWindowHeight(getHeight());
                store.setCommitWindowConfig(windows);
            }
        });

        //语言下拉列表显示
        Constant.LANGUAGES.forEach(optionLanguage::addItem);
        optionLanguage.addActionListener(e -> {
            optionLanguage.hidePopup();
            var language = CommUtil.convertLanguageDomain(optionLanguage);
            if (!language.equals(store.getLanguage())) {
                store.setLanguage(language);
                handleDisplayLanguageEvent(language);
            }
        });

        //设置窗口打开位置为屏幕中心
        var dataContext = DataManager.getInstance().getDataContext(this);
        var project = CommonDataKeys.PROJECT.getData(dataContext);
        setLocationRelativeTo(null);
        var parentWindow = WindowManager.getInstance().getFrame(project);
        if (parentWindow != null) setLocationRelativeTo(parentWindow);

        //设置git提交更改范围历史记录
        var scopeList = CommUtil.loadGitCommitScopeHistory(project);
        scopeList.forEach(optionScopeChange::addItem);
    }

    /**
     * 重置弹窗信息
     */
    public void resetUIFrom(GitCommitDomain gitCommit) {
        //提交类型回显
        if (gitCommit.getCommitType() != null) {
            var commitTypeList = CommUtil.getDefaultCommitTypeList();
            var buttonElements = typeChangeGroup.getElements();
            while (buttonElements.hasMoreElements()) {
                var button = buttonElements.nextElement();
                var index = Integer.parseInt(button.getActionCommand());
                var changeType = commitTypeList.get(index - 1);

                if (changeType.getType().equalsIgnoreCase(gitCommit.getCommitType().getType())) {
                    button.setSelected(true);
                    break;
                }
            }
        }

        //语言类型回显
        optionLanguage.setSelectedItem(store.getLanguage());
        handleDisplayLanguageEvent(store.getLanguage());

        //文本内容回显
        optionScopeChange.setSelectedItem(gitCommit.getChangeScope());
        optionScopeChange.setSelectedItem(gitCommit.getChangeScope());
        inputShortDescription.setText(gitCommit.getShortDescription());
        inputLongDescription.setText(gitCommit.getLongDescription());
        inputClosedIssues.setText(gitCommit.getClosedIssuesNumbers());
        inputBreakingChanges.setText(gitCommit.getBreakingChanges());
        checkBoxSkipCI.setSelected(gitCommit.isSkipCI());
        checkBoxWrapText.setSelected(gitCommit.isWrapText());
    }

    /**
     * 处理语言显示事件
     *
     * @param language 语言对象
     */
    private void handleDisplayLanguageEvent(LanguageDomain language) {
        var resourceBundle = CommUtil.i18nResourceBundle(language.getKey());

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

        //渲染提交类型按钮组信息
        var commitTypeList = CommUtil.getDefaultCommitTypeList();
        var buttonElements = typeChangeGroup.getElements();
        buttonElements.nextElement();
        while (buttonElements.hasMoreElements()) {
            var button = buttonElements.nextElement();
            button.setVisible(false);
            var index = Integer.parseInt(button.getActionCommand());
            if (index > commitTypeList.size()) continue;

            button.setVisible(true);
            var commitType = commitTypeList.get(index - 1);
            button.setActionCommand(commitType.getType());
            button.setText(commitType.getType() + " - " + commitType.getDescription());
        }

        //渲染窗口宽度信息
        var width = 880;
        var height = 700;
        width = switch (language.getKey()) {
            case "de_DE" -> 950;
            case "it_IT" -> 960;
            case "fr_FR", "fr_CA" -> 1100;
            default -> 880;
        };
        //比对存储的窗口大小
        var window = store.getCommitWindowConfig();
        if (window == null) window = new WindowsConfigDomain();

        //设置窗口宽高
        var storeWidth = window.getWindowWidth();
        if (storeWidth < width) window.setWindowWidth(width);
        var storeHeight = window.getWindowHeight();
        if (storeHeight < height) window.setWindowHeight(height);
        setSize(new Dimension(storeWidth, storeHeight));
        setMinimumSize(new Dimension(width, height));

        //设置窗口坐标
        var x = window.getWindowX();
        var y = window.getWindowY();
        if (x != -1 && y != -1) setBounds(x, y, storeWidth, storeHeight);
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
        radioButton1 = new JRadioButton();
        radioButton1.setActionCommand("1");
        radioButton1.setSelected(true);
        radioButton1.setText("feat - A new feature");
        panel4.add(radioButton1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton2 = new JRadioButton();
        radioButton2.setActionCommand("2");
        radioButton2.setSelected(false);
        radioButton2.setText("fix - A bug fix");
        panel4.add(radioButton2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton3 = new JRadioButton();
        radioButton3.setActionCommand("3");
        radioButton3.setText("docs - Documentation only changes");
        panel4.add(radioButton3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton4 = new JRadioButton();
        radioButton4.setActionCommand("4");
        radioButton4.setText("style - Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)");
        panel4.add(radioButton4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton5 = new JRadioButton();
        radioButton5.setActionCommand("5");
        radioButton5.setText("refactor - A code change that neither fixes a bug nor adds a feature");
        panel4.add(radioButton5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton6 = new JRadioButton();
        radioButton6.setActionCommand("6");
        radioButton6.setSelected(false);
        radioButton6.setText("perf - A code change that improves performance");
        panel4.add(radioButton6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton7 = new JRadioButton();
        radioButton7.setActionCommand("7");
        radioButton7.setText("test - Adding missing tests or correcting existing tests");
        panel4.add(radioButton7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton8 = new JRadioButton();
        radioButton8.setActionCommand("8");
        radioButton8.setText("build - Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)");
        panel4.add(radioButton8, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton9 = new JRadioButton();
        radioButton9.setActionCommand("9");
        radioButton9.setText("ci - Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)");
        panel4.add(radioButton9, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton10 = new JRadioButton();
        radioButton10.setActionCommand("10");
        radioButton10.setText("chore - Other changes that don't modify src or test files");
        panel4.add(radioButton10, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton11 = new JRadioButton();
        radioButton11.setActionCommand("11");
        radioButton11.setText("revert - Reverts a previous commit");
        panel4.add(radioButton11, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelLanguage = new JLabel();
        labelLanguage.setText("Language");
        panel3.add(labelLanguage, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        optionLanguage = new JComboBox();
        panel3.add(optionLanguage, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        typeChangeGroup = new ButtonGroup();
        typeChangeGroup.add(radioButton1);
        typeChangeGroup.add(radioButton1);
        typeChangeGroup.add(radioButton2);
        typeChangeGroup.add(radioButton3);
        typeChangeGroup.add(radioButton4);
        typeChangeGroup.add(radioButton5);
        typeChangeGroup.add(radioButton6);
        typeChangeGroup.add(radioButton7);
        typeChangeGroup.add(radioButton8);
        typeChangeGroup.add(radioButton9);
        typeChangeGroup.add(radioButton10);
        typeChangeGroup.add(radioButton11);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
