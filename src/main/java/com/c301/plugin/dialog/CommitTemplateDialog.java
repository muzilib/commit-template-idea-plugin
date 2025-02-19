package com.c301.plugin.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    private JComboBox comboBox1;
    private JCheckBox wrapTextCheckBox;
    private JCheckBox skipCICheckBox;
    private JTextField shortDescription;
    private JLabel typeOfChangeLabel;
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

    public CommitTemplateDialog(Project project) {
        setTitle("提交模板");
        setModal(true);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        //设置显示窗口大小
        pack();
        setMinimumSize(new Dimension(700, 600));

        //设置窗口打开位置为屏幕中心
        setLocationRelativeTo(null);
        var parentWindow = WindowManager.getInstance().getFrame(project);
        if (parentWindow != null) setLocationRelativeTo(parentWindow);


        buttonOK.addActionListener(e -> handleOKEvent());
        buttonCancel.addActionListener(e -> handleCancelEvent());

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
        // add your code here
        dispose();
    }

    /**
     * 处理取消事件
     */
    private void handleCancelEvent() {
        // add your code here if necessary
        dispose();
    }

}
