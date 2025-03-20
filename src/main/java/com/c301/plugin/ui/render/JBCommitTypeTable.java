package com.c301.plugin.ui.render;

import com.c301.plugin.config.StoreCommitTemplateState;
import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.CommitTypeDomain;
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

    private final StoreCommitTemplateState store;

    public JBCommitTypeTable(StoreCommitTemplateState store) {
        this.store = store;

        setModel(new CommitTypeTableModel(store));
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

    /**
     * 新增 事件
     */
    public void handlesAddActionEvent() {
        var customList = store.getCustomCommitTypeList();

        //检查是否超过最大数量
        if (customList.size() >= Constant.MAX_COMMIT_TYPE_LENGTH) {
            JOptionPane.showMessageDialog(this, "模板类型已添加最大上线，请删除或修改已有记录。", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        var dialog = new EditCommitTypeDialog(store, false);
        dialog.setVisible(true);
    }

    /**
     * 修改 事件
     */
    public void handlesEditActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;

        var customList = store.getCustomCommitTypeList();
        var data = customList.get(selectRows[0]);
        var dialog = new EditCommitTypeDialog(store, true);
        dialog.setVisible(true);
    }

    /**
     * 移除 事件
     */
    public void handlesRemoveActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;
        var index = selectRows[0];

//        DATA_LIST.remove(index);
//        TABLE_MODEL.fireTableDataChanged();
    }

    /**
     * 上移 事件
     */
    public void handlesMoveUpActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;
        var index = selectRows[0];
        if (index == 0) return;

//        var temp = DATA_LIST.get(index - 1);
//        DATA_LIST.set(index - 1, DATA_LIST.get(index));
//        DATA_LIST.set(index, temp);
//        TABLE_MODEL.fireTableDataChanged();
        setRowSelectionInterval(index - 1, index - 1);
    }

    /**
     * 下移 事件
     */
    public void handlesMoveDownActionEvent() {
        var selectRows = getSelectedRows();
        if (selectRows.length != 1) return;
        var index = selectRows[0];
//        if (index == DATA_LIST.size() - 1) return;

//        var temp = DATA_LIST.get(index + 1);
//        DATA_LIST.set(index + 1, DATA_LIST.get(index));
//        DATA_LIST.set(index, temp);
//        TABLE_MODEL.fireTableDataChanged();
        setRowSelectionInterval(index + 1, index + 1);
    }

    /**
     * 处理数据新增或更新事件
     *
     * @param data 数据
     */
    public static void handleCommitTypeDataEvent(CommitTypeDomain data) {
        /*ChangeTypeDomain changeTypeDomain = null;
        for (var item : DATA_LIST) {
            if (item.getName().equals(data.getName())) {
                changeTypeDomain = item;
                break;
            }
        }

        if (changeTypeDomain != null) {
            changeTypeDomain.setName(data.getName());
            changeTypeDomain.setDirection(data.getDirection());
        } else DATA_LIST.add(data);
        TABLE_MODEL.fireTableDataChanged();*/
    }

}
