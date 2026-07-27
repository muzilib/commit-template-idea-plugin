package com.c301.plugin.ui;

import com.c301.plugin.config.CommitTemplateSettingsResolver;
import com.c301.plugin.config.EffectiveCommitTemplateSettings;
import com.c301.plugin.config.PluginUiLanguageSettings;

import com.c301.plugin.config.StoreCommitTemplateState;
import com.c301.plugin.constant.Constant;
import com.c301.plugin.domain.commit.CommitMessageFormatter;
import com.c301.plugin.domain.commit.CommitMessageValidator;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitCommitDomain;
import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.model.WindowsConfigDomain;

import com.c301.plugin.utils.CommUtil;
import com.c301.plugin.utils.StrUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private JLabel labelCommitTypeNoData;
    private JLabel labelCommitTypeSetting;
    private ButtonGroup typeChangeGroup;
    private JLabel labelPreview;
    private JTextArea previewCommitMessage;

    private final StoreCommitTemplateState store = StoreCommitTemplateState.getInstance();
    private final CommitMessageI commitMessageI;
    private final Project project;
    private final EffectiveCommitTemplateSettings effectiveSettings;

    /**
     * 创建弹窗信息
     */
    public CommitTemplateDialog(CommitMessageI commitMessageI, Project project) {
        $$$setupUI$$$();
        setModal(true);
        this.commitMessageI = commitMessageI;
        this.project = project;
        this.effectiveSettings = resolveEffectiveSettings(project);
        setContentPane(createDialogContent());
        getRootPane().setDefaultButton(buttonOK);
        labelCommitTypeNoData.setVisible(false);
        labelCommitTypeSetting.setVisible(false);
        optionScopeChange.setFont(Constant.EMOJI_FONT);
        inputShortDescription.setFont(Constant.EMOJI_FONT);
        inputLongDescription.setFont(Constant.EMOJI_FONT);
        inputBreakingChanges.setFont(Constant.EMOJI_FONT);
        previewCommitMessage.setFont(Constant.EMOJI_FONT);
        installPreviewListeners();

        //设置显示窗口大小
        pack();
        setMinimumSize(new Dimension(880, 560));

        buttonOK.addActionListener(e -> handleOKEvent());
        buttonCancel.addActionListener(e -> handleCancelEvent());
        labelLanguage.setVisible(false);
        optionLanguage.setVisible(false);

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
            var gitCommit = createCommitFromForm();
            var validation = CommitMessageValidator.validate(gitCommit.getCommitType(), gitCommit.getChangeScope(),
                    inputShortDescription.getText(), effectiveSettings.commitMessageRules());
            if (!validation.isValid()) {
                var resourceBundle = CommUtil.i18nResourceBundle(null);
                JOptionPane.showMessageDialog(this, validationMessage(resourceBundle, validation),
                        resourceBundle.getString("plugin.setting.dialog.warning"), JOptionPane.WARNING_MESSAGE);
                return;
            }

            commitMessageI.setCommitMessage(formatCommit(gitCommit));
        }

        handleCancelEvent();
    }

    /**
     * 处理取消事件
     */
    private void handleCancelEvent() {
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



        //设置窗口打开位置为屏幕中心
        setLocationRelativeTo(null);
        if (project != null) {
            var parentWindow = WindowManager.getInstance().getFrame(project);
            if (parentWindow != null) setLocationRelativeTo(parentWindow);

            // 后台读取 Git 历史，避免大仓库阻塞提交窗口。
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                var scopeList = CommUtil.loadGitCommitScopeHistory(project);
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (!isDisplayable()) {
                        return;
                    }
                    scopeList.forEach(optionScopeChange::addItem);
                });
            });
        }
    }

    /**
     * 重置弹窗信息
     */
    public void resetUIFrom(GitCommitDomain gitCommit) {
        //提交类型回显
        if (gitCommit.getCommitType() != null) {
            var buttonElements = typeChangeGroup.getElements();
            while (buttonElements.hasMoreElements()) {
                var button = buttonElements.nextElement();
                if (button.getActionCommand().equalsIgnoreCase(gitCommit.getCommitType().getType())) {
                    button.setSelected(true);
                    break;
                }
            }
        }

        handleDisplayLanguageEvent(PluginUiLanguageSettings.resolve(store));

        //文本内容回显
        optionScopeChange.setSelectedItem(gitCommit.getChangeScope());
        inputShortDescription.setText(gitCommit.getShortDescription());
        inputLongDescription.setText(gitCommit.getLongDescription());
        inputClosedIssues.setText(gitCommit.getClosedIssuesNumbers());
        inputBreakingChanges.setText(gitCommit.getBreakingChanges());
        checkBoxSkipCI.setSelected(gitCommit.isSkipCI());
        checkBoxWrapText.setSelected(gitCommit.isWrapText());
        refreshPreview();
    }

    /**
     * 检查窗口位置是否在屏幕范围内
     *
     * @param x      窗口X坐标
     * @param y      窗口Y坐标
     * @param width  窗口宽度
     * @param height 窗口高度
     * @return 是否在屏幕范围内
     */
    private boolean isWindowInScreenBounds(int x, int y, int width, int height) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        for (GraphicsDevice screen : screens) {
            Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
            if (screenBounds.contains(x, y) &&
                    screenBounds.contains(x + width, y) &&
                    screenBounds.contains(x, y + height) &&
                    screenBounds.contains(x + width, y + height)) {
                return true;
            }
        }
        return false;
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
        labelPreview.setText(resourceBundle.getString("plugin.label.preview"));

        //复选框信息
        checkBoxWrapText.setText(resourceBundle.getString("plugin.checkbox.wrapAt72Characters"));
        checkBoxSkipCI.setText(resourceBundle.getString("plugin.checkbox.skipCI"));

        //提示信息
        labelCommitTypeNoData.setText(resourceBundle.getString("plugin.setting.label.tipsNoData"));
        labelCommitTypeSetting.setText(resourceBundle.getString("plugin.setting.label.tipsGoSetting"));

        //渲染提交类型按钮组信息
        var commitTypeList = CommUtil.getDefaultCommitTypeList(effectiveSettings.language().getKey());
        if (effectiveSettings.customEnable()) commitTypeList = effectiveSettings.customCommitTypeList();
        var buttonElements = typeChangeGroup.getElements();
        if (commitTypeList.isEmpty()) {
            while (buttonElements.hasMoreElements()) {
                var button = buttonElements.nextElement();
                button.setVisible(false);
            }

            // 添加鼠标点击监听和打开设置界面
            labelCommitTypeNoData.setVisible(true);
            labelCommitTypeSetting.setVisible(true);
            labelCommitTypeSetting.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            labelCommitTypeSetting.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleCancelEvent();
                    Project targetProject = project;
                    if (targetProject == null) {
                        var openProjects = ProjectManager.getInstance().getOpenProjects();
                        if (openProjects.length > 0) {
                            targetProject = openProjects[0];
                        }
                    }
                    if (targetProject != null) {
                        ShowSettingsUtil.getInstance().showSettingsDialog(targetProject,
                                com.c301.plugin.config.UnifiedCommitTemplateSettingsConfigurable.class);
                    }
                }
            });
        } else {
            labelCommitTypeNoData.setVisible(false);
            labelCommitTypeSetting.setVisible(false);
            var index = 0;
            while (buttonElements.hasMoreElements()) {
                var button = buttonElements.nextElement();
                button.setSelected(false);
                button.setVisible(false);
                if (index >= commitTypeList.size()) continue;

                button.setVisible(true);
                button.setFont(Constant.EMOJI_FONT);
                var commitType = commitTypeList.get(index++);
                button.setActionCommand(commitType.getType());
                button.setText(commitType.toString(effectiveSettings.emojiEnable()));
            }
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
        refreshPreview();

        if (x != -1 && y != -1) {
            if (isWindowInScreenBounds(x, y, storeWidth, storeHeight)) {
                setBounds(x, y, storeWidth, storeHeight);
            } else {
                // 如果窗口位置不在屏幕范围内，则显示在父窗口中心
                if (project != null) {
                    var parentWindow = WindowManager.getInstance().getFrame(project);
                    if (parentWindow != null) {
                        setLocationRelativeTo(parentWindow);
                    }
                }
            }
        }
    }

    private String validationMessage(java.util.ResourceBundle resourceBundle,
                                     CommitMessageValidator.ValidationResult validation) {
        return switch (validation) {
            case MISSING_SCOPE -> resourceBundle.getString("plugin.dialog.error.scopeRequired");
            case SUBJECT_TOO_LONG -> resourceBundle.getString("plugin.dialog.error.subjectTooLong")
                    .replace("{max}", String.valueOf(effectiveSettings.commitMessageRules().subjectMaxLength()));
            case SUBJECT_TRAILING_PERIOD -> resourceBundle.getString("plugin.dialog.error.subjectTrailingPeriod");
            default -> resourceBundle.getString("plugin.dialog.error.commitRequired");
        };
    }

    private JPanel createDialogContent() {
        var dialogContent = new JPanel(new BorderLayout(0, 8));
        dialogContent.add(contentPane, BorderLayout.CENTER);

        var previewPanel = new JPanel(new BorderLayout(0, 6));
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(""),
                BorderFactory.createEmptyBorder(2, 6, 6, 6)));
        previewPanel.setPreferredSize(new Dimension(0, 108));
        previewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        labelPreview = new JLabel();
        labelPreview.setFont(labelPreview.getFont().deriveFont(Font.BOLD));
        previewCommitMessage = new JTextArea(3, 0);
        previewCommitMessage.setEditable(false);
        previewCommitMessage.setLineWrap(true);
        previewCommitMessage.setWrapStyleWord(true);
        previewCommitMessage.setRows(3);
        var previewScrollPane = new JScrollPane(previewCommitMessage);
        previewScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        previewPanel.add(labelPreview, BorderLayout.NORTH);
        previewPanel.add(previewScrollPane, BorderLayout.CENTER);
        if (effectiveSettings.previewEnabled()) {
            dialogContent.add(previewPanel, BorderLayout.SOUTH);
        }
        return dialogContent;
    }

    private void installPreviewListeners() {
        var documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshPreview();
            }
        };
        inputShortDescription.getDocument().addDocumentListener(documentListener);
        inputLongDescription.getDocument().addDocumentListener(documentListener);
        inputBreakingChanges.getDocument().addDocumentListener(documentListener);
        inputClosedIssues.getDocument().addDocumentListener(documentListener);
        ((JTextField) optionScopeChange.getEditor().getEditorComponent()).getDocument().addDocumentListener(documentListener);
        optionScopeChange.addActionListener(e -> refreshPreview());
        checkBoxWrapText.addActionListener(e -> refreshPreview());
        checkBoxSkipCI.addActionListener(e -> refreshPreview());

        var buttons = typeChangeGroup.getElements();
        while (buttons.hasMoreElements()) {
            buttons.nextElement().addActionListener(e -> refreshPreview());
        }
    }

    private void refreshPreview() {
        if (previewCommitMessage == null) {
            return;
        }
        var commit = createCommitFromForm();
        var resourceBundle = CommUtil.i18nResourceBundle(null);
        if (!CommitMessageValidator.validate(commit.getCommitType(), commit.getChangeScope(),
                commit.getShortDescription(), effectiveSettings.commitMessageRules()).isValid()) {
            previewCommitMessage.setText(resourceBundle.getString("plugin.preview.empty"));
            return;
        }
        previewCommitMessage.setText(formatCommit(commit));
        previewCommitMessage.setCaretPosition(0);
    }

    private GitCommitDomain createCommitFromForm() {
        var gitCommit = new GitCommitDomain();
        gitCommit.setCommitType(selectedCommitType());
        gitCommit.setChangeScope(optionScopeChange.getSelectedItem() == null ? "" : optionScopeChange.getSelectedItem().toString());
        gitCommit.setShortDescription(inputShortDescription.getText());
        gitCommit.setLongDescription(inputLongDescription.getText());
        gitCommit.setBreakingChanges(inputBreakingChanges.getText());
        gitCommit.setClosedIssues(parseClosedIssues(inputClosedIssues.getText()));
        gitCommit.setWrapText(checkBoxWrapText.isSelected());
        gitCommit.setSkipCI(checkBoxSkipCI.isSelected());
        return gitCommit;
    }

    private CommitTypeDomain selectedCommitType() {
        var buttons = typeChangeGroup.getElements();
        while (buttons.hasMoreElements()) {
            var button = buttons.nextElement();
            if (!button.isSelected()) {
                continue;
            }
            return effectiveSettings.customEnable()
                    ? effectiveSettings.customCommitTypeList().stream()
                    .filter(item -> item.getType().equals(button.getActionCommand()))
                    .findFirst()
                    .orElse(null)
                    : CommUtil.getDefaultCommitTypeList(effectiveSettings.language().getKey()).stream()
                    .filter(item -> item.getType().equals(button.getActionCommand()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private String formatCommit(GitCommitDomain commit) {
        var location = effectiveSettings.emojiEnable() ? effectiveSettings.emojiLocation() : null;
        return CommitMessageFormatter.format(commit, location, effectiveSettings.commitMessageRules());
    }

    private LinkedList<Integer> parseClosedIssues(String input) {
        var issues = new LinkedList<Integer>();
        if (StrUtil.isBlank(input)) {
            return issues;
        }
        for (String item : input.trim().replace('，', ',').split(",")) {
            String issue = item.trim().replace("#", "");
            if (StrUtil.isNumeric(issue)) {
                issues.add(Integer.parseInt(issue));
            }
        }
        return issues;
    }



    private EffectiveCommitTemplateSettings resolveEffectiveSettings(Project project) {
        if (project != null) {
            return CommitTemplateSettingsResolver.getInstance(project).resolve();
        }
        return new EffectiveCommitTemplateSettings(
                store.getLanguage(),
                store.isCustomEnable(),
                store.isEmojiEnable(),
                store.getEmojiLocation(),
                CommUtil.deepCopy(store.getCustomCommitTypeList()),
                store.getCommitMessageRules() == null
                        ? com.c301.plugin.domain.commit.CommitMessageRules.defaults()
                        : store.getCommitMessageRules().toDomain(),
                store.isPreviewEnabled()
        );
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
        panel4.setLayout(new GridLayoutManager(12, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        radioButton1 = new JRadioButton();
        radioButton1.setActionCommand("1");
        Font radioButton1Font = UIManager.getFont("Label.font");
        if (radioButton1Font != null) radioButton1.setFont(radioButton1Font);
        radioButton1.setMargin(new Insets(2, 2, 2, 2));
        radioButton1.setSelected(true);
        radioButton1.setText("feat - A new feature");
        radioButton1.setVisible(true);
        panel4.add(radioButton1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton2 = new JRadioButton();
        radioButton2.setActionCommand("2");
        Font radioButton2Font = UIManager.getFont("Label.font");
        if (radioButton2Font != null) radioButton2.setFont(radioButton2Font);
        radioButton2.setMargin(new Insets(2, 2, 2, 2));
        radioButton2.setSelected(false);
        radioButton2.setText("fix - A bug fix");
        radioButton2.setVisible(true);
        panel4.add(radioButton2, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton3 = new JRadioButton();
        radioButton3.setActionCommand("3");
        Font radioButton3Font = UIManager.getFont("Label.font");
        if (radioButton3Font != null) radioButton3.setFont(radioButton3Font);
        radioButton3.setMargin(new Insets(2, 2, 2, 2));
        radioButton3.setText("docs - Documentation only changes");
        radioButton3.setVisible(true);
        panel4.add(radioButton3, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton4 = new JRadioButton();
        radioButton4.setActionCommand("4");
        Font radioButton4Font = UIManager.getFont("Label.font");
        if (radioButton4Font != null) radioButton4.setFont(radioButton4Font);
        radioButton4.setMargin(new Insets(2, 2, 2, 2));
        radioButton4.setText("style - Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)");
        radioButton4.setVisible(true);
        panel4.add(radioButton4, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton5 = new JRadioButton();
        radioButton5.setActionCommand("5");
        Font radioButton5Font = UIManager.getFont("Label.font");
        if (radioButton5Font != null) radioButton5.setFont(radioButton5Font);
        radioButton5.setMargin(new Insets(2, 2, 2, 2));
        radioButton5.setText("refactor - A code change that neither fixes a bug nor adds a feature");
        radioButton5.setVisible(true);
        panel4.add(radioButton5, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton6 = new JRadioButton();
        radioButton6.setActionCommand("6");
        Font radioButton6Font = UIManager.getFont("Label.font");
        if (radioButton6Font != null) radioButton6.setFont(radioButton6Font);
        radioButton6.setMargin(new Insets(2, 2, 2, 2));
        radioButton6.setSelected(false);
        radioButton6.setText("perf - A code change that improves performance");
        radioButton6.setVisible(true);
        panel4.add(radioButton6, new GridConstraints(6, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton7 = new JRadioButton();
        radioButton7.setActionCommand("7");
        Font radioButton7Font = UIManager.getFont("Label.font");
        if (radioButton7Font != null) radioButton7.setFont(radioButton7Font);
        radioButton7.setMargin(new Insets(2, 2, 2, 2));
        radioButton7.setText("test - Adding missing tests or correcting existing tests");
        radioButton7.setVisible(true);
        panel4.add(radioButton7, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton8 = new JRadioButton();
        radioButton8.setActionCommand("8");
        Font radioButton8Font = UIManager.getFont("Label.font");
        if (radioButton8Font != null) radioButton8.setFont(radioButton8Font);
        radioButton8.setMargin(new Insets(2, 2, 2, 2));
        radioButton8.setText("build - Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)");
        radioButton8.setVisible(true);
        panel4.add(radioButton8, new GridConstraints(8, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton9 = new JRadioButton();
        radioButton9.setActionCommand("9");
        Font radioButton9Font = UIManager.getFont("Label.font");
        if (radioButton9Font != null) radioButton9.setFont(radioButton9Font);
        radioButton9.setMargin(new Insets(2, 2, 2, 2));
        radioButton9.setText("ci - Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)");
        radioButton9.setVisible(true);
        panel4.add(radioButton9, new GridConstraints(9, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton10 = new JRadioButton();
        radioButton10.setActionCommand("10");
        Font radioButton10Font = UIManager.getFont("Label.font");
        if (radioButton10Font != null) radioButton10.setFont(radioButton10Font);
        radioButton10.setMargin(new Insets(2, 2, 2, 2));
        radioButton10.setText("chore - Other changes that don't modify src or test files");
        radioButton10.setVisible(true);
        panel4.add(radioButton10, new GridConstraints(10, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        radioButton11 = new JRadioButton();
        radioButton11.setActionCommand("11");
        Font radioButton11Font = UIManager.getFont("Label.font");
        if (radioButton11Font != null) radioButton11.setFont(radioButton11Font);
        radioButton11.setMargin(new Insets(2, 2, 2, 2));
        radioButton11.setText("revert - Reverts a previous commit");
        radioButton11.setVisible(true);
        panel4.add(radioButton11, new GridConstraints(11, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelCommitTypeNoData = new JLabel();
        labelCommitTypeNoData.setEnabled(true);
        Font labelCommitTypeNoDataFont = UIManager.getFont("Label.font");
        if (labelCommitTypeNoDataFont != null) labelCommitTypeNoData.setFont(labelCommitTypeNoDataFont);
        labelCommitTypeNoData.setText("没有配置\"提交类型\"，请前往设置页面配置: ");
        panel4.add(labelCommitTypeNoData, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        labelCommitTypeSetting = new JLabel();
        Font labelCommitTypeSettingFont = UIManager.getFont("Label.font");
        if (labelCommitTypeSettingFont != null) labelCommitTypeSetting.setFont(labelCommitTypeSettingFont);
        labelCommitTypeSetting.setForeground(new Color(-13273872));
        labelCommitTypeSetting.setText("前往设置");
        panel4.add(labelCommitTypeSetting, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
