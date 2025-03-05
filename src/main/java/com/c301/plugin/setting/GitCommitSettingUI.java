package com.c301.plugin.setting;

import com.c301.plugin.dialog.render.LanguageListCellRendererRender;
import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.model.StoreConfig;
import com.c301.plugin.setting.render.CommitTypeTable;
import com.c301.plugin.utils.CommUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static com.c301.plugin.constant.Constant.OPTINS_LANGUAGE_LIST;

/**
 * Git提交设置面板
 *
 * @Title GitCommitSettingUI
 * @ClassName com.c301.plugin.setting.GitCommitSettingUI
 * @Author Chenbing
 * @Date 25 /03/05 08:52
 * @Version 1.0
 */
public class GitCommitSettingUI {

    private JPanel editCommitTypePanel;
    private CommitTypeTable commitTypeTable;

    private JPanel mainPanel;
    private JLabel imageIcon;
    private JPanel aboutPanel;
    private JPanel settingPanel;
    private JLabel labelLanguage;
    private JPanel typeTablePanel;
    private JLabel labelCommitType;
    private JTabbedPane tabbedPane;
    private JCheckBox checkBoxCommitType;
    private JComboBox<LanguageDomain> optionLanguage;

    private final StoreConfig storeConfig = StoreCommitTemplateState.getInstance().storeConfig;

    public GitCommitSettingUI() {
        //初始化Logo信息
        var url = this.getClass().getResource("/META-INF/pluginIcon.png");
        var icon = new ImageIcon(Objects.requireNonNull(url));
        var image = icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH);
        icon.setImage(image);
        imageIcon.setIcon(icon);

        //设置语言模板
        optionLanguage.addActionListener(e -> {
            var languageDomain = CommUtil.convertLanguageDomain(optionLanguage);
            handleDisplayLanguageEvent(languageDomain.getKey());
        });
        optionLanguage.setRenderer(new LanguageListCellRendererRender());
        OPTINS_LANGUAGE_LIST.forEach(optionLanguage::addItem);

        //设置自定义模板事件
        checkBoxCommitType.addChangeListener(e -> {
            var enable = checkBoxCommitType.isSelected();
            commitTypeTable.clearSelection();
            typeTablePanel.setEnabled(enable);
            editCommitTypePanel.setEnabled(enable);
        });

        //设置主界面
        if (commitTypeTable == null) commitTypeTable = new CommitTypeTable();
        var labelFont = UIManager.getFont("Label.font");
        if (labelFont != null) commitTypeTable.setFont(labelFont);
        if (editCommitTypePanel == null) {
            editCommitTypePanel = ToolbarDecorator.createDecorator(commitTypeTable)
                    .setAddAction(button -> commitTypeTable.handlesAddActionEvent())
                    .setRemoveAction(button -> commitTypeTable.handlesRemoveActionEvent())
                    .setEditAction(button -> commitTypeTable.handlesEditActionEvent())
                    .setMoveUpAction(button -> commitTypeTable.handlesMoveUpActionEvent())
                    .setMoveDownAction(button -> commitTypeTable.handlesMoveDownActionEvent())
                    .createPanel();
        }
        typeTablePanel.add(editCommitTypePanel, BorderLayout.CENTER);

        //回显设置状态
        checkBoxCommitType.setSelected(storeConfig.templateEnable);
        optionLanguage.setSelectedItem(CommUtil.convertLanguageDomain(storeConfig.language));
    }

    /**
     * 处理语言显示事件
     *
     * @param languageKey 语言Key
     */
    private void handleDisplayLanguageEvent(String languageKey) {
        var resourceBundle = CommUtil.i18nResourceBundle(languageKey);

        //显示语言控制
        tabbedPane.setTitleAt(0, resourceBundle.getString("plugin.setting.label.setting"));
        tabbedPane.setTitleAt(1, resourceBundle.getString("plugin.setting.label.about"));
        labelLanguage.setText(resourceBundle.getString("plugin.setting.label.language"));
        labelCommitType.setText(resourceBundle.getString("plugin.setting.label.template"));
        if (commitTypeTable != null) {
            var title = new String[]{
                    resourceBundle.getString("plugin.setting.label.typeName"),
                    resourceBundle.getString("plugin.setting.label.typeDescribe")
            };
            commitTypeTable.flush(title);
        }
    }

    /**
     * 数据是否修改
     *
     * @return true: 修改
     */
    public boolean isModified() {
        //语言是否调整
        var defaultValue = StoreCommitTemplateState.getInstance().defaultValue;
        var languageDomain = CommUtil.convertLanguageDomain(optionLanguage);
        if (!defaultValue.language.equalsIgnoreCase(languageDomain.getKey())) {
            return true;
        }

        //是否开启了模板功能
        return checkBoxCommitType.isSelected() != defaultValue.templateEnable;
    }

    /**
     * 重置数据模板
     */
    public void reset() {
        var defaultValue = StoreCommitTemplateState.getInstance().defaultValue;
        checkBoxCommitType.setSelected(defaultValue.templateEnable);

        storeConfig.language = defaultValue.language;
        storeConfig.commitTypeList = defaultValue.commitTypeList;
        storeConfig.templateEnable = defaultValue.templateEnable;
    }

    /**
     * 获取主面板
     *
     * @return main panel
     */
    public JPanel getMainPanel() {
        return mainPanel;
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        settingPanel = new JPanel();
        settingPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("设置", settingPanel);
        typeTablePanel = new JPanel();
        typeTablePanel.setLayout(new BorderLayout(0, 0));
        typeTablePanel.setEnabled(false);
        typeTablePanel.setToolTipText("");
        settingPanel.add(typeTablePanel, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        optionLanguage = new JComboBox();
        Font optionLanguageFont = UIManager.getFont("Label.font");
        if (optionLanguageFont != null) optionLanguage.setFont(optionLanguageFont);
        settingPanel.add(optionLanguage, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelLanguage = new JLabel();
        Font labelLanguageFont = UIManager.getFont("Label.font");
        if (labelLanguageFont != null) labelLanguage.setFont(labelLanguageFont);
        labelLanguage.setText("显示语言");
        settingPanel.add(labelLanguage, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBoxCommitType = new JCheckBox();
        Font checkBoxCommitTypeFont = UIManager.getFont("Label.font");
        if (checkBoxCommitTypeFont != null) checkBoxCommitType.setFont(checkBoxCommitTypeFont);
        checkBoxCommitType.setText("使用自定义的Git提交类型");
        settingPanel.add(checkBoxCommitType, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelCommitType = new JLabel();
        Font labelCommitTypeFont = UIManager.getFont("Label.font");
        if (labelCommitTypeFont != null) labelCommitType.setFont(labelCommitTypeFont);
        labelCommitType.setText("编辑模板");
        settingPanel.add(labelCommitType, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        aboutPanel = new JPanel();
        aboutPanel.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("关于", aboutPanel);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        aboutPanel.add(panel1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        imageIcon = new JLabel();
        panel1.add(imageIcon, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(128, 128), new Dimension(128, 128), new Dimension(128, 128), 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        aboutPanel.add(panel2, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = UIManager.getFont("Label.font");
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("构建版本");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = UIManager.getFont("Label.font");
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("VX.X.X");
        panel2.add(label2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        Font label3Font = UIManager.getFont("Label.font");
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("(EAP/STAND)");
        panel2.add(label3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        aboutPanel.add(panel3, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        Font label4Font = UIManager.getFont("Label.font");
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("构建时间");
        panel3.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        Font label5Font = UIManager.getFont("Label.font");
        if (label5Font != null) label5.setFont(label5Font);
        label5.setText("YYYYMMddHHmm");
        panel3.add(label5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        aboutPanel.add(panel4, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        Font label6Font = UIManager.getFont("Label.font");
        if (label6Font != null) label6.setFont(label6Font);
        label6.setText("Powered by _Chenbing");
        panel4.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel4.add(spacer4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        aboutPanel.add(spacer5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
