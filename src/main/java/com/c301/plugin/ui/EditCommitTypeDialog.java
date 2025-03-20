package com.c301.plugin.ui;

import com.c301.plugin.config.StoreCommitTemplateState;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.ui.render.JBCommitTypeTable;
import com.c301.plugin.utils.CommUtil;
import com.c301.plugin.utils.StrUtil;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.stream.Collectors;

/**
 * 编辑commitType 对话框
 *
 * @Title EditCommitType
 * @ClassName com.c301.plugin.config.EditCommitType
 * @Author Chenbing
 * @Date 25/03/05 11:55
 * @Version 1.0
 **/
public class EditCommitTypeDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> inputType;
    private JTextArea inputDescribe;
    private JLabel labelName;
    private JLabel labelDescribe;

    private int index = -1;
    private final JBCommitTypeTable table;
    private CommitTypeDomain commitType;
    private final StoreCommitTemplateState store;

    public EditCommitTypeDialog(StoreCommitTemplateState store, JBCommitTypeTable table, CommitTypeDomain commitType) {
        this.store = store;
        this.table = table;
        this.commitType = commitType;

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setPreferredSize(new Dimension(400, 220));
        setMinimumSize(new Dimension(400, 220));
        pack();
        setModal(true);

        //设置窗口打开位置为屏幕中心
        setLocationRelativeTo(null);
        var parentWindow = WindowManager.getInstance().getMostRecentFocusedWindow();
        if (parentWindow != null) setLocationRelativeTo(parentWindow);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        //设置类型列表
        inputType.addItem("");
        var typeNameArrays = store.getCustomCommitTypeList().stream()
                .map(item -> item.getType().toLowerCase())
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        for (String typeName : CommitTypeDomain.TYPES) {
            if (typeNameArrays.contains(typeName)) continue;
            inputType.addItem(typeName);
        }
        inputType.addActionListener(e -> {
            inputDescribe.setText("");

            //设置选中类型描述
            var item = inputType.getSelectedItem();
            if (item == null) return;

            //获取选中类型
            var typeName = item.toString();
            if (CommitTypeDomain.TYPES.contains(typeName)) {
                var resourceBundle = CommUtil.i18nResourceBundle(store.getLanguage().getKey());
                inputDescribe.setText(resourceBundle.getString("plugin.radio." + typeName));
            }
        });

        //切换语言事件
        handleDisplayLanguageEvent(store.getLanguage());
    }

    /**
     * 点击确定按钮
     */
    private void onOK() {
        var typeOption = inputType.getSelectedItem();
        if (typeOption == null || StrUtil.isBlank(typeOption.toString())) {
            JOptionPane.showMessageDialog(this, "请输入/选择类型名称", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (StrUtil.isBlank(inputDescribe.getText())) {
            JOptionPane.showMessageDialog(this, "请输入类型描述", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //新增事件
        var customList = store.getCustomCommitTypeList();
        if (commitType == null) {
            var domain = new CommitTypeDomain();
            domain.setType(typeOption.toString());
            domain.setDescription(inputDescribe.getText());
            customList.add(domain);
            table.handleRefreshEvent();
            onCancel();
            return;
        }

        //修改事件
        if (index == -1) return;

        //找到修改的行索引，判断typeName是否重复
        for (int i = 0; i < customList.size(); i++) {
            var item = customList.get(i);
            if (item.getType().equals(typeOption.toString())) {
                if (i == index) continue;
                JOptionPane.showMessageDialog(this, "类型名称已存在，请重新输入", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        var domain = customList.get(index);
        domain.setType(typeOption.toString());
        domain.setDescription(inputDescribe.getText());
        table.handleRefreshEvent();
        onCancel();
    }

    /**
     * 点击取消
     */
    private void onCancel() {
        dispose();
    }

    /**
     * 重置表单信息
     *
     * @param commitType 提交类型对象
     * @param index      选中的行索引
     */
    public void resetUIFrom(CommitTypeDomain commitType, int index) {
        this.index = index;
        this.commitType = commitType;
        if (commitType == null) return;

        inputType.setSelectedItem(commitType.getType());
        inputDescribe.setText(commitType.getDescription());
    }

    /**
     * 显示语言切换事件
     *
     * @param language 语言对象
     */
    private void handleDisplayLanguageEvent(LanguageDomain language) {
        var resourceBundle = CommUtil.i18nResourceBundle(language.getKey());

        buttonOK.setText(resourceBundle.getString("plugin.button.ok"));
        buttonCancel.setText(resourceBundle.getString("plugin.button.cancel"));
        labelName.setText(resourceBundle.getString("plugin.setting.table.typeName"));
        labelDescribe.setText(resourceBundle.getString("plugin.setting.table.typeDescribe"));

        //设置标题名称
        var titleKey = commitType != null ? "edit" : "add";
        setTitle(resourceBundle.getString("plugin.setting.dialog.title." + titleKey));
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
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelName = new JLabel();
        Font labelNameFont = UIManager.getFont("Label.font");
        if (labelNameFont != null) labelName.setFont(labelNameFont);
        labelName.setText("类型名称");
        panel3.add(labelName, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        inputType = new JComboBox();
        inputType.setEditable(true);
        Font inputTypeFont = UIManager.getFont("Label.font");
        if (inputTypeFont != null) inputType.setFont(inputTypeFont);
        panel3.add(inputType, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelDescribe = new JLabel();
        Font labelDescribeFont = UIManager.getFont("Label.font");
        if (labelDescribeFont != null) labelDescribe.setFont(labelDescribeFont);
        labelDescribe.setText("类型描述");
        panel3.add(labelDescribe, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(1, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        inputDescribe = new JTextArea();
        Font inputDescribeFont = UIManager.getFont("Label.font");
        if (inputDescribeFont != null) inputDescribe.setFont(inputDescribeFont);
        scrollPane1.setViewportView(inputDescribe);
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
