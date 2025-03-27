package com.c301.plugin.config;

import com.c301.plugin.model.GitmojiDomain;
import com.c301.plugin.ui.render.GitmojiVcsLogIconCellRenderer;
import com.c301.plugin.utils.CommUtil;
import com.intellij.vcs.log.ui.table.GraphTableModel;
import com.intellij.vcs.log.ui.table.VcsLogGraphTable;
import com.intellij.vcs.log.ui.table.column.VcsLogCustomColumn;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellRenderer;

/**
 * 自定义 GitLog日志中 gitmoji 渲染列<br/>
 * 参考项目：<a href="https://github.com/patou/gitmoji-intellij-plugin">gitmoji-intellij-plugin</a>
 *
 * @Title GitmojiVcsLogCustomColumn
 * @ClassName com.c301.plugin.config.GitmojiVcsLogCustomColumn
 * @Author Chenbing
 * @Date 25/03/21 16:19
 * @Version 1.0
 **/
public class GitmojiVcsLogCustomColumn implements VcsLogCustomColumn<GitmojiDomain> {

    private final GitmojiDomain DEFAULT_GITMOJI = new GitmojiDomain("anguished", "anguished", "😧", "anguished");

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull String getId() {
        return "gitmoji";
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @Nls @NotNull String getLocalizedName() {
        return "Gitmoji";
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean isEnabledByDefault() {
        //判断是否开启了gitmoji
        var store = StoreCommitTemplateState.getInstance();
        return store.isEmojiEnable();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public boolean isResizable() {
        return false;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @Nullable GitmojiDomain getValue(@NotNull GraphTableModel model, int row) {
        CommUtil.handleInitGitmojiEvent();
        var content = model.getCommitMetadata(row).getFullMessage();
        for (GitmojiDomain item : GitmojiDomain.GITMOJIS) {
            if (content.contains(item.getCode()) || content.contains(item.getEmoji())) {
                return item;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull TableCellRenderer createTableCellRenderer(@NotNull VcsLogGraphTable vcsLogGraphTable) {
        return new GitmojiVcsLogIconCellRenderer();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public GitmojiDomain getStubValue(@NotNull GraphTableModel graphTableModel) {
        return DEFAULT_GITMOJI;
    }

}