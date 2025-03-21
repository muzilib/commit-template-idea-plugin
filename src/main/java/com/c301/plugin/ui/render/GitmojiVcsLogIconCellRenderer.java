package com.c301.plugin.ui.render;

import com.c301.plugin.model.GitmojiDomain;
import com.intellij.vcs.log.ui.table.VcsLogGraphTable;
import com.intellij.vcs.log.ui.table.VcsLogIconCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 自定义 Gitmoji 渲染器
 *
 * @Title GitmojiVcsLogIconCellRenderer
 * @ClassName com.c301.plugin.ui.render.GitmojiVcsLogIconCellRenderer
 * @Author Chenbing
 * @Date 25/03/21 16:28
 * @Version 1.0
 **/
public class GitmojiVcsLogIconCellRenderer extends VcsLogIconCellRenderer {

    @Override
    protected void customize(@NotNull VcsLogGraphTable vcsLogGraphTable, @Nullable Object o, boolean b, boolean b1, int i, int i1) {
        if (o == null) return;
        if (o instanceof GitmojiDomain gitmoji) {
            //加载失败的图片
            if (Objects.equals(gitmoji.getName(), "anguished")) {
                append("-");
                return;
            }

            //正常加载的图片
            setIcon(gitmoji.getIcon());
            setToolTipText(gitmoji.getName());
        }
    }

}