package com.c301.plugin.setting.render;

import org.apache.commons.text.StringEscapeUtils;

import javax.swing.table.AbstractTableModel;

/**
 * 提交类型表格 标题类型
 *
 * @Title CommitTypeTableModel
 * @ClassName com.c301.plugin.setting.render.CommitTypeTableModel
 * @Author Chenbing
 * @Date 25/03/05 10:36
 * @Version 1.0
 **/
public class CommitTypeTableModel extends AbstractTableModel {

    public static final int NAME_COLUMN = 0;
    public static final int VALUE_COLUMN = 1;
    private static final String[] HEADERS = new String[]{"", ""};

    @Override
    public int getRowCount() {
        return CommitTypeTable.getDataList().size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var domain = CommitTypeTable.getDataList().get(rowIndex);
        if (columnIndex == NAME_COLUMN) return StringEscapeUtils.unescapeJava(domain.getName());
        if (columnIndex == VALUE_COLUMN) return StringEscapeUtils.unescapeJava(domain.getDirection());
        return "";
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == NAME_COLUMN) return HEADERS[NAME_COLUMN];
        if (columnIndex == VALUE_COLUMN) return HEADERS[VALUE_COLUMN];
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    /**
     * 设置标题
     *
     * @param titles 标题集合
     */
    public void setColumnTitles(String[] titles) {
        HEADERS[NAME_COLUMN] = titles[NAME_COLUMN];
        HEADERS[VALUE_COLUMN] = titles[VALUE_COLUMN];
        fireTableStructureChanged();
    }

}
