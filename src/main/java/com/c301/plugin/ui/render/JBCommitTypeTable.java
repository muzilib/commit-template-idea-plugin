package com.c301.plugin.ui.render;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.model.SettingCacheDomain;
import com.c301.plugin.ui.EditCommitTypeDialog;
import com.c301.plugin.utils.CommUtil;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;

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
        setMinimumSize(new Dimension(0, 0));
        handleRefreshEvent();
    }

    /**
     * The settings dialog may be narrower than a localized description.  The table must follow
     * its viewport instead of requesting its content width and forcing the settings dialog to
     * show a horizontal scrollbar.
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public void doLayout() {
        super.doLayout();
        updateRowHeights();
    }

    /**
     * Imports missing built-in types and refreshes existing built-in descriptions for the requested
     * commit-content language. Types not supplied by the built-in list are left untouched.
     *
     * @return number of inserted or refreshed entries, limited by the configured maximum.
     */
    public int importSystemDefaults(LanguageDomain language) {
        var existingTypes = new HashMap<String, com.c301.plugin.model.CommitTypeDomain>();
        for (var commitType : cache.getCustomCommitTypeList()) {
            if (commitType != null && commitType.getType() != null) {
                existingTypes.put(commitType.getType(), commitType);
            }
        }

        int changed = 0;
        for (var defaultType : CommUtil.getDefaultCommitTypeList(language.getKey())) {
            var existing = existingTypes.get(defaultType.getType());
            if (existing == null) {
                if (cache.getCustomCommitTypeList().size() >= Constant.MAX_COMMIT_TYPE_LENGTH) {
                    break;
                }
                cache.getCustomCommitTypeList().add(CommUtil.deepCopy(defaultType));
                changed++;
                continue;
            }
            if (!java.util.Objects.equals(existing.getDescription(), defaultType.getDescription())
                    || !java.util.Objects.equals(existing.getEmoji(), defaultType.getEmoji())) {
                existing.setDescription(defaultType.getDescription());
                existing.setEmoji(defaultType.getEmoji());
                changed++;
            }
        }
        handleRefreshEvent();
        return changed;
    }

    /**
     * 新增 事件
     */
    public void handlesAddActionEvent() {
        var customList = cache.getCustomCommitTypeList();

        //检查是否超过最大数量
        if (customList.size() >= Constant.MAX_COMMIT_TYPE_LENGTH) {
            var resourceBundle = CommUtil.i18nResourceBundle(null);
            var label = resourceBundle.getString("plugin.setting.dialog.warning");
            var message = resourceBundle.getString("plugin.setting.dialog.maxlength");
            JOptionPane.showMessageDialog(this, message, label, JOptionPane.WARNING_MESSAGE);
            return;
        }

        var dialog = new EditCommitTypeDialog(cache, this, null);
        dialog.handleUIInit();
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
        dialog.handleUIInit();
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
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setRowHeight(40);

        if (cache.isEmojiEnable()) {
            var gitmojiColumn = getColumnModel().getColumn(CommitTypeTableModel.GITMOJE_COLUMN);
            gitmojiColumn.setMinWidth(28);
            gitmojiColumn.setMaxWidth(48);
            gitmojiColumn.setPreferredWidth(36);
        }

        var typeColumn = getColumnModel().getColumn(CommitTypeTableModel.TYPE_COLUMN);
        typeColumn.setMinWidth(56);
        typeColumn.setPreferredWidth(100);
        typeColumn.setMaxWidth(140);

        // The description column is deliberately allowed to shrink. Its renderer wraps text and
        // updateRowHeights() grows the affected row rather than widening the settings page.
        var descriptionColumn = getColumnModel().getColumn(CommitTypeTableModel.DESCRIPTION_COLUMN);
        descriptionColumn.setMinWidth(0);
        descriptionColumn.setPreferredWidth(320);
        descriptionColumn.setMaxWidth(Integer.MAX_VALUE);
        updateRowHeights();
    }

    private void updateRowHeights() {
        if (getRowCount() == 0 || getColumnModel().getColumnCount() == 0) {
            return;
        }
        int descriptionColumn = CommitTypeTableModel.DESCRIPTION_COLUMN;
        int width = getColumnModel().getColumn(descriptionColumn).getWidth();
        if (width <= 0) {
            return;
        }
        for (int row = 0; row < getRowCount(); row++) {
            TableCellRenderer renderer = getCellRenderer(row, descriptionColumn);
            Component component = prepareRenderer(renderer, row, descriptionColumn);
            component.setSize(width, Short.MAX_VALUE);
            int height = Math.max(40, component.getPreferredSize().height);
            if (getRowHeight(row) != height) {
                setRowHeight(row, height);
            }
        }
    }

}
