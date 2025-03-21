package com.c301.plugin.ui;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.model.SettingCacheDomain;
import com.c301.plugin.ui.render.CustomTableCellRenderer;
import com.c301.plugin.ui.render.JBCommitTypeTable;
import com.c301.plugin.ui.render.LanguageListCellRendererRender;
import com.c301.plugin.utils.CommUtil;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * Git提交设置面板
 *
 * @Title CommitTemplateSettingUI
 * @ClassName com.c301.plugin.config.CommitTemplateSettingUI
 * @Author Chenbing
 * @Date 25 /03/05 08:52
 * @Version 1.0
 */
@Getter
public class CommitTemplateSettingUI {

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
    private JCheckBox checkBoxGitmoji;
    private JLabel labelGitmoji;
    private JLabel labelGitmojiWebsite;

    private final SettingCacheDomain cache;

    public CommitTemplateSettingUI(SettingCacheDomain cache) {
        this.cache = cache;
        var commitTypeTable = new JBCommitTypeTable(cache);

        //初始化语言列表
        optionLanguage.setRenderer(new LanguageListCellRendererRender());
        Constant.LANGUAGES.forEach(optionLanguage::addItem);
        optionLanguage.addActionListener(e -> {
            optionLanguage.hidePopup();
            var language = CommUtil.convertLanguageDomain(optionLanguage);
            if (!language.equals(cache.getLanguage())) {
                cache.setLanguage(language);
                handleDisplayLanguageEvent(language);
                commitTypeTable.handleRefreshEvent();
            }
        });

        //设置提交类型编辑面板
        commitTypeTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        var editCommitTypePanel = ToolbarDecorator.createDecorator(commitTypeTable)
                .setAddAction(button -> commitTypeTable.handlesAddActionEvent())
                .setRemoveAction(button -> commitTypeTable.handlesRemoveActionEvent())
                .setEditAction(button -> commitTypeTable.handlesEditActionEvent())
                .setMoveUpAction(button -> commitTypeTable.handlesMoveUpActionEvent())
                .setMoveDownAction(button -> commitTypeTable.handlesMoveDownActionEvent())
                .createPanel();
        typeTablePanel.add(editCommitTypePanel, BorderLayout.CENTER);

        //设置自定义语言模板开启状态
        checkBoxCommitType.addItemListener(e -> {
            var enable = (e.getStateChange() == ItemEvent.SELECTED);

            cache.setCustomEnable(enable);
            typeTablePanel.setEnabled(enable);
            commitTypeTable.setEnabled(enable);
            editCommitTypePanel.setEnabled(enable);

            //自定义提交模板描述信息
            var resourceBundle = CommUtil.i18nResourceBundle(null);
            var active = enable ? "active" : "deActive";
            checkBoxCommitType.setText(resourceBundle.getString("plugin.setting.label.customTemplateTips." + active));
        });

        //自定义提交模板开启状态
        checkBoxGitmoji.addItemListener(e -> {
            var enable = (e.getStateChange() == ItemEvent.SELECTED);
            cache.setEmojiEnable(enable);

            //自定义提交模板描述信息
            var resourceBundle = CommUtil.i18nResourceBundle(null);
            var active = enable ? "active" : "deActive";
            checkBoxGitmoji.setText(resourceBundle.getString("plugin.setting.label.customTemplateTips." + active));
        });

        //添加双击行元素事件
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent e) {
                commitTypeTable.handlesEditActionEvent();
                return true;
            }
        }.installOn(commitTypeTable);

        //初始化Logo信息
        var url = this.getClass().getResource("/META-INF/pluginIcon.png");
        var icon = new ImageIcon(Objects.requireNonNull(url));
        var image = icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH);
        icon.setImage(image);
        imageIcon.setIcon(icon);
    }

    /**
     * 处理页面重置事件
     */
    public void handleResetEvent(SettingCacheDomain cache) {
        optionLanguage.setSelectedItem(cache.getLanguage());
        checkBoxCommitType.setSelected(cache.isCustomEnable());
        checkBoxGitmoji.setSelected(cache.isEmojiEnable());
        this.cache.setCustomCommitTypeList(cache.getCustomCommitTypeList());

        handleDisplayLanguageEvent(cache.getLanguage());
    }

    /**
     * 处理语言显示事件
     *
     * @param language 语言对象
     */
    private void handleDisplayLanguageEvent(LanguageDomain language) {
        var resourceBundle = CommUtil.i18nResourceBundle(language.getKey());

        //显示语言控制
        tabbedPane.setTitleAt(0, resourceBundle.getString("plugin.setting.label.setting"));
        tabbedPane.setTitleAt(1, resourceBundle.getString("plugin.setting.label.about"));
        labelLanguage.setText(resourceBundle.getString("plugin.setting.label.language"));
        labelCommitType.setText(resourceBundle.getString("plugin.setting.label.customTemplate"));

        //自定义提交模板描述信息
        var active = checkBoxCommitType.isSelected() ? "active" : "deActive";
        checkBoxCommitType.setText(resourceBundle.getString("plugin.setting.label.customTemplateTips." + active));
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
        settingPanel.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("设置", settingPanel);
        typeTablePanel = new JPanel();
        typeTablePanel.setLayout(new BorderLayout(0, 0));
        typeTablePanel.setEnabled(false);
        typeTablePanel.setToolTipText("");
        settingPanel.add(typeTablePanel, new GridConstraints(3, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        optionLanguage = new JComboBox();
        Font optionLanguageFont = UIManager.getFont("Label.font");
        if (optionLanguageFont != null) optionLanguage.setFont(optionLanguageFont);
        settingPanel.add(optionLanguage, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelLanguage = new JLabel();
        Font labelLanguageFont = UIManager.getFont("Label.font");
        if (labelLanguageFont != null) labelLanguage.setFont(labelLanguageFont);
        labelLanguage.setText("显示语言");
        settingPanel.add(labelLanguage, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBoxCommitType = new JCheckBox();
        Font checkBoxCommitTypeFont = UIManager.getFont("Label.font");
        if (checkBoxCommitTypeFont != null) checkBoxCommitType.setFont(checkBoxCommitTypeFont);
        checkBoxCommitType.setText("使用自定义的Git提交类型");
        settingPanel.add(checkBoxCommitType, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelCommitType = new JLabel();
        Font labelCommitTypeFont = UIManager.getFont("Label.font");
        if (labelCommitTypeFont != null) labelCommitType.setFont(labelCommitTypeFont);
        labelCommitType.setText("编辑模板");
        settingPanel.add(labelCommitType, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelGitmoji = new JLabel();
        Font labelGitmojiFont = UIManager.getFont("Label.font");
        if (labelGitmojiFont != null) labelGitmoji.setFont(labelGitmojiFont);
        labelGitmoji.setText("Git Emoji");
        settingPanel.add(labelGitmoji, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBoxGitmoji = new JCheckBox();
        Font checkBoxGitmojiFont = UIManager.getFont("Label.font");
        if (checkBoxGitmojiFont != null) checkBoxGitmoji.setFont(checkBoxGitmojiFont);
        checkBoxGitmoji.setText("使用Gitmoji符号");
        settingPanel.add(checkBoxGitmoji, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        settingPanel.add(spacer1, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        labelGitmojiWebsite = new JLabel();
        Font labelGitmojiWebsiteFont = UIManager.getFont("Label.font");
        if (labelGitmojiWebsiteFont != null) labelGitmojiWebsite.setFont(labelGitmojiWebsiteFont);
        labelGitmojiWebsite.setForeground(new Color(-13273872));
        labelGitmojiWebsite.setText("Gitmoji 网站");
        settingPanel.add(labelGitmojiWebsite, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        aboutPanel = new JPanel();
        aboutPanel.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("关于", aboutPanel);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        aboutPanel.add(panel1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        imageIcon = new JLabel();
        panel1.add(imageIcon, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(128, 128), new Dimension(128, 128), new Dimension(128, 128), 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
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
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
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
        final Spacer spacer4 = new Spacer();
        panel3.add(spacer4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        aboutPanel.add(panel4, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        Font label6Font = UIManager.getFont("Label.font");
        if (label6Font != null) label6.setFont(label6Font);
        label6.setText("Powered by _Chenbing");
        panel4.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel4.add(spacer5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        aboutPanel.add(spacer6, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
