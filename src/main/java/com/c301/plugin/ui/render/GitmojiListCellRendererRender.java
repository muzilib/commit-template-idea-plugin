package com.c301.plugin.ui.render;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.GitmojiDomain;

import javax.swing.*;
import java.awt.*;

/**
 * 自定义渲染Gitmoji选项
 *
 * @Title GitmojiListCellRendererRender
 * @ClassName com.c301.plugin.ui.render.GitmojiListCellRendererRender
 * @Author Chenbing
 * @Date 25/03/25 17:56
 * @Version 1.0
 **/
public class GitmojiListCellRendererRender extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // 显示 label 字段
        if (value instanceof GitmojiDomain domain) {
            var label = domain.getEmoji();
            label += " (" + domain.getName() + ")";
            setText(label);
            setFont(Constant.EMOJI_FONT);
        }
        return this;
    }

}