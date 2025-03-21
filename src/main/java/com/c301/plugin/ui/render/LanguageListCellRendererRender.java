package com.c301.plugin.ui.render;

import com.c301.plugin.model.LanguageDomain;

import javax.swing.*;
import java.awt.*;

/**
 * 自定义渲染语言选项
 *
 * @Title LanguageRender
 * @ClassName com.c301.plugin.ui.render.LanguageListCellRendererRender
 * @Author Chenbing
 * @Date 25/02/19 15:48
 * @Version 1.0
 **/
public class LanguageListCellRendererRender extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // 显示 label 字段
        if (value instanceof LanguageDomain language) {
            var label = language.getLabel();
            label += " (" + language.getKey() + ")";
            setText(label);
        }
        return this;
    }

}
