package com.c301.plugin.setting.render;

import com.c301.plugin.utils.StrUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 自定义表单文字渲染，支持中文
 *
 * @Title CustomTableCellRenderer
 * @ClassName com.c301.plugin.setting.render.CustomTableCellRenderer
 * @Author Chenbing
 * @Date 25/03/07 16:25
 * @Version 1.0
 **/
public class CustomTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        var component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        //处理字符串乱码
        if (value instanceof String) {
            ((JLabel) component).setText(StrUtil.unicodeToString((String) value));
        }

        //设置渲染字体
        if (component instanceof JLabel) {
            var font = UIManager.getFont("Label.font");
            if (font != null) component.setFont(font);
        }
        return component;
    }

}
