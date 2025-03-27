package com.c301.plugin.ui.render;

import com.c301.plugin.model.SettingCacheDomain;
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

    public static int GITMOJE_COLUMN = -1;
    public static int TYPE_COLUMN = -1;
    public static int DESCRIPTION_COLUMN = -1;

    private final SettingCacheDomain cache;

    public CommitTypeTableModel(SettingCacheDomain cache) {
        TYPE_COLUMN = 0;
        DESCRIPTION_COLUMN = 1;

        if (cache.isEmojiEnable()) {
            GITMOJE_COLUMN = 0;
            TYPE_COLUMN = 1;
            DESCRIPTION_COLUMN = 2;
        }
        this.cache = cache;
    }

    @Override
    public int getRowCount() {
        return cache.getCustomCommitTypeList().size();
    }

    @Override
    public int getColumnCount() {
        return cache.isEmojiEnable() ? 3 : 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var dataList = cache.getCustomCommitTypeList();

        //获取数据对象
        var domain = dataList.get(rowIndex);
        if (cache.isEmojiEnable() && columnIndex == GITMOJE_COLUMN) {
            if (domain.getEmoji() == null) return "";
            return domain.getEmoji().getEmoji();
        }
        if (columnIndex == TYPE_COLUMN) return domain.getType();
        if (columnIndex == DESCRIPTION_COLUMN) return domain.getDescription();
        return null;
    }

    @Override
    public String getColumnName(int columnIndex) {
        var language = cache.getLanguage();
        var resourceBundle = CommUtil.i18nResourceBundle(language.getKey());

        if (cache.isEmojiEnable() && columnIndex == GITMOJE_COLUMN) return "Gitmoji";
        if (columnIndex == TYPE_COLUMN) return resourceBundle.getString("plugin.setting.table.typeName");
        if (columnIndex == DESCRIPTION_COLUMN) return resourceBundle.getString("plugin.setting.table.typeDescribe");
        return "None";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

}
