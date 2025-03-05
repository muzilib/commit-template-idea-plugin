package com.c301.plugin.setting.render;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.ChangeTypeDomain;
import com.c301.plugin.setting.EditCommitTypeDialog;
import com.c301.plugin.setting.StoreCommitTemplateState;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import java.util.List;

/**
 * 提交类型表格渲染组件
 *
 * @Title CommitTypeTable
 * @ClassName com.c301.plugin.setting.render.CommitTypeTable
 * @Author Chenbing
 * @Date 25/03/05 10:34
 * @Version 1.0
 **/
public class CommitTypeTable extends JBTable {
    private static final Logger log = Logger.getInstance(CommitTypeTable.class);
    private static final List<ChangeTypeDomain> commitTypeList = StoreCommitTemplateState.getInstance().storeConfig.commitTypeList;
    private static final CommitTypeTableModel TABLE_MODEL = new CommitTypeTableModel(commitTypeList);

    public CommitTypeTable() {
        setModel(TABLE_MODEL);
        var nameColumn = getColumnModel().getColumn(CommitTypeTableModel.NAME_COLUMN);
        nameColumn.setMinWidth(150);
        nameColumn.setPreferredWidth(150);
        nameColumn.setMaxWidth(250);

        var valueColumn = getColumnModel().getColumn(CommitTypeTableModel.VALUE_COLUMN);
        valueColumn.setMinWidth(550);
        valueColumn.setPreferredWidth(550);
        valueColumn.setMaxWidth(750);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 设置表头
        var resourceBundle = CommUtil.i18nResourceBundle(null);
        var title = new String[]{resourceBundle.getString("plugin.setting.label.typeName"), resourceBundle.getString("plugin.setting.label.typeDescribe")};
        TABLE_MODEL.setColumnTitles(title);
    }

    /**
     * 新增 事件
     */
    public void handlesAddActionEvent() {
        if (commitTypeList.size() >= Constant.MAX_COMMIT_TYPE_LENGTH) {
            JOptionPane.showMessageDialog(this, "模板类型已添加最大上线，请删除或修改已有记录。", "错误", JOptionPane.ERROR_MESSAGE);
            log.warn("提交类型已达到最大值 " + Constant.MAX_COMMIT_TYPE_LENGTH);
            return;
        }

        var dialog = new EditCommitTypeDialog(null);
        dialog.setVisible(true);
    }

    /**
     * 修改 事件
     */
    public void handlesEditActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;

        var data = commitTypeList.get(selectRows[0]);
        var dialog = new EditCommitTypeDialog(data);
        dialog.setVisible(true);
    }

    /**
     * 移除 事件
     */
    public void handlesRemoveActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;
        var index = selectRows[0];

        commitTypeList.remove(index);
        TABLE_MODEL.fireTableDataChanged();
    }

    /**
     * 上移 事件
     */
    public void handlesMoveUpActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;
        var index = selectRows[0];
        if (index == 0) return;

        var temp = commitTypeList.get(index - 1);
        commitTypeList.set(index - 1, commitTypeList.get(index));
        commitTypeList.set(index, temp);
        TABLE_MODEL.fireTableDataChanged();
        setRowSelectionInterval(index - 1, index - 1);
    }

    /**
     * 下移 事件
     */
    public void handlesMoveDownActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;
        var index = selectRows[0];
        if (index == commitTypeList.size() - 1) return;

        var temp = commitTypeList.get(index + 1);
        commitTypeList.set(index + 1, commitTypeList.get(index));
        commitTypeList.set(index, temp);
        TABLE_MODEL.fireTableDataChanged();
        setRowSelectionInterval(index + 1, index + 1);
    }

    /**
     * 处理数据新增或更新事件
     *
     * @param data 数据
     */
    public static void handleCommitTypeDataEvent(ChangeTypeDomain data) {
        ChangeTypeDomain changeTypeDomain = null;
        for (var item : commitTypeList) {
            if (item.getName().equals(data.getName())) {
                changeTypeDomain = item;
                break;
            }
        }

        if (changeTypeDomain != null) {
            changeTypeDomain.setName(data.getName());
            changeTypeDomain.setDirection(data.getDirection());
        } else commitTypeList.add(data);
        TABLE_MODEL.fireTableDataChanged();
    }

    public void flush(String[] titles) {
        TABLE_MODEL.setColumnTitles(titles);
    }

}
