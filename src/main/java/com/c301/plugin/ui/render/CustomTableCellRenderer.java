package com.c301.plugin.ui.render;

import com.c301.plugin.constant.Constant;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 自定义表单文字渲染，支持中文
 *
 * @Title CustomTableCellRenderer
 * @ClassName com.c301.plugin.ui.render.CustomTableCellRenderer
 * @Author Chenbing
 * @Date 25/03/07 16:25
 * @Version 1.0
 **/
public class CustomTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (column == CommitTypeTableModel.DESCRIPTION_COLUMN) {
            JTextArea description = new JTextArea(value == null ? "" : value.toString());
            description.setFont(Constant.EMOJI_FONT);
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setRows(2);
            description.setOpaque(true);
            description.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            description.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            description.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            return description;
        }

        var component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (component instanceof JLabel label) {
            label.setText(value == null ? "" : value.toString());
            label.setFont(Constant.EMOJI_FONT);
        }
        return component;
    }
}
