package com.c301.plugin.ui.render;

import com.c301.plugin.config.StoreCommitTemplateState;
import com.c301.plugin.utils.CommUtil;

import javax.swing.table.AbstractTableModel;

/**
 * 提交类型表格 标题类型
 *
 * @Title CommitTypeTableModel
 * @ClassName com.c301.plugin.ui.render.CommitTypeTableModel
 * @Author Chenbing
 * @Date 25/03/05 10:36
 * @Version 1.0
 **/
public class CommitTypeTableModel extends AbstractTableModel {

    public static final int TYPE_COLUMN = 0;
    public static final int DESCRIPTION_COLUMN = 1;

    private final StoreCommitTemplateState store;

    public CommitTypeTableModel(StoreCommitTemplateState store) {
        this.store = store;
    }

    @Override
    public int getRowCount() {
        return store.getCustomCommitTypeList().size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var dataList = store.getCustomCommitTypeList();

        //获取数据对象
        var domain = dataList.get(rowIndex);
        if (columnIndex == TYPE_COLUMN) return domain.getType();
        if (columnIndex == DESCRIPTION_COLUMN) return domain.getDescription();
        return null;
    }

    @Override
    public String getColumnName(int columnIndex) {
        var language = store.getLanguage();
        var resourceBundle = CommUtil.i18nResourceBundle(language.getKey());

        if (columnIndex == TYPE_COLUMN) resourceBundle.getString("plugin.setting.label.typeName");
        if (columnIndex == DESCRIPTION_COLUMN) resourceBundle.getString("plugin.setting.label.typeDescribe");
        return "None";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

}
