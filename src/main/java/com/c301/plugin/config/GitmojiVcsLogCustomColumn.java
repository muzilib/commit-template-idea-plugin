package com.c301.plugin.config;

import com.c301.plugin.model.GitmojiDomain;
import com.c301.plugin.ui.render.GitmojiVcsLogIconCellRenderer;
import com.intellij.vcs.log.ui.table.GraphTableModel;
import com.intellij.vcs.log.ui.table.VcsLogGraphTable;
import com.intellij.vcs.log.ui.table.column.VcsLogCustomColumn;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellRenderer;

/**
 * 自定义 gitmoji 渲染列
 *
 * @Title GitmojiVcsLogCustomColumn
 * @ClassName com.c301.plugin.config.GitmojiVcsLogCustomColumn
 * @Author Chenbing
 * @Date 25/03/21 16:19
 * @Version 1.0
 **/
public class GitmojiVcsLogCustomColumn implements VcsLogCustomColumn<GitmojiDomain> {

    @Override
    public @NotNull String getId() {
        return "gitmoji";
    }

    @Override
    public @Nls @NotNull String getLocalizedName() {
        return "Gitmoji";
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    @Override
    public @Nullable GitmojiDomain getValue(@NotNull GraphTableModel model, int row) {
        var content = model.getCommitMetadata(row).getFullMessage();
        /*if (content.contains(":") && content.contains(":")) {
            var index = content.indexOf(":");
            var code = content.substring(0, index);
            var name = content.substring(index + 1, content.length());
            return new GitmojiDomain(code, name, "", "");
        }*/
        return null;
    }

    @Override
    public @NotNull TableCellRenderer createTableCellRenderer(@NotNull VcsLogGraphTable vcsLogGraphTable) {
        return new GitmojiVcsLogIconCellRenderer();
    }

    @Override
    public GitmojiDomain getStubValue(@NotNull GraphTableModel graphTableModel) {
        return new GitmojiDomain("anguished", "anguished", "😧", "anguished");
    }

}