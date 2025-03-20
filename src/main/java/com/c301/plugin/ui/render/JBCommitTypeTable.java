package com.c301.plugin.ui.render;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.SettingCacheDomain;
import com.c301.plugin.ui.EditCommitTypeDialog;
import com.intellij.ui.table.JBTable;

import javax.swing.*;

/**
 * 提交类型表格渲染组件
 *
 * @Title CommitTypeTable
 * @ClassName com.c301.plugin.ui.render.CommitTypeTable
 * @Author Chenbing
 * @Date 25/03/05 10:34
 * @Version 1.0
 **/
public class JBCommitTypeTable extends JBTable {

    private final SettingCacheDomain cache;

    public JBCommitTypeTable(SettingCacheDomain cache) {
        this.cache = cache;
        handleRefreshEvent();
    }

    /**
     * 新增 事件
     */
    public void handlesAddActionEvent() {
        var customList = cache.getCustomCommitTypeList();

        //检查是否超过最大数量
        if (customList.size() >= Constant.MAX_COMMIT_TYPE_LENGTH) {
            JOptionPane.showMessageDialog(this, "模板类型已添加最大上线，请删除或修改已有记录。", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        var dialog = new EditCommitTypeDialog(cache, this, null);
        dialog.setVisible(true);
    }

    /**
     * 修改 事件
     */
    public void handlesEditActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;

        var arrays = cache.getCustomCommitTypeList();
        var commitType = arrays.get(selectRows[0]);

        var dialog = new EditCommitTypeDialog(cache, this, commitType);
        dialog.resetUIFrom(commitType, selectRows[0]);
        dialog.setVisible(true);
    }

    /**
     * 移除 事件
     */
    public void handlesRemoveActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;

        var index = selectRows[0];
        cache.getCustomCommitTypeList().remove(index);
        handleRefreshEvent();
    }

    /**
     * 上移 事件
     */
    public void handlesMoveUpActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;
        var index = selectRows[0];
        if (index == 0) return;

        var arrays = cache.getCustomCommitTypeList();
        var temp = arrays.get(index - 1);
        arrays.set(index - 1, arrays.get(index));
        arrays.set(index, temp);
        setRowSelectionInterval(index - 1, index - 1);
        handleRefreshEvent();
    }

    /**
     * 下移 事件
     */
    public void handlesMoveDownActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;
        var index = selectRows[0];

        var arrays = cache.getCustomCommitTypeList();
        if (index == arrays.size() - 1) return;

        var temp = arrays.get(index + 1);
        arrays.set(index + 1, arrays.get(index));
        arrays.set(index, temp);
        setRowSelectionInterval(index + 1, index + 1);
        handleRefreshEvent();
    }

    /**
     * 刷新页面事件
     */
    public void handleRefreshEvent() {
        setModel(new CommitTypeTableModel(cache));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //设置列宽 提交类型 分类
        var typeColumn = getColumnModel().getColumn(CommitTypeTableModel.TYPE_COLUMN);
        typeColumn.setMinWidth(150);
        typeColumn.setMaxWidth(250);
        typeColumn.setPreferredWidth(150);

        //设置列宽 提交类型 描述
        var descriptionColumn = getColumnModel().getColumn(CommitTypeTableModel.DESCRIPTION_COLUMN);
        descriptionColumn.setMinWidth(550);
        descriptionColumn.setMaxWidth(750);
        descriptionColumn.setPreferredWidth(550);
    }

}
