package com.c301.plugin.config;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.ui.render.LanguageListCellRendererRender;
import com.c301.plugin.utils.CommUtil;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Global configuration for plugin UI language, kept separate from commit-content language.
 */
final class PluginUiLanguageConfigurable {
    private final StoreCommitTemplateState state = StoreCommitTemplateState.getInstance();
    private JPanel panel;
    private JLabel description;
    private JCheckBox syncWithIde;
    private JLabel uiLanguageLabel;
    private JComboBox<LanguageDomain> uiLanguage;

    private static GridBagConstraints constraints(int y) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = y;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = JBUI.insets(4, 0);
        return constraints;
    }

    JComponent createComponent() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            GridBagConstraints constraints = constraints(0);

            description = new JLabel();
            description.setForeground(UIManager.getColor("Label.disabledForeground"));
            panel.add(description, constraints);
            constraints.gridy++;
            syncWithIde = new JCheckBox();
            panel.add(syncWithIde, constraints);
            constraints.gridy++;
            constraints.insets = JBUI.insets(8, 24, 2, 0);
            uiLanguageLabel = new JLabel();
            panel.add(uiLanguageLabel, constraints);
            constraints.gridy++;
            constraints.insets = JBUI.insets(2, 24, 4, 0);
            uiLanguage = new JComboBox<>();
            uiLanguage.setRenderer(new LanguageListCellRendererRender());
            Constant.LANGUAGES.forEach(uiLanguage::addItem);
            panel.add(uiLanguage, constraints);

            GridBagConstraints filler = constraints(constraints.gridy + 1);
            filler.weighty = 1;
            filler.fill = GridBagConstraints.VERTICAL;
            panel.add(Box.createVerticalGlue(), filler);
            syncWithIde.addActionListener(e -> updateEnabledState());
            refreshTexts();
        }
        reset();
        return panel;
    }

    boolean isModified() {
        return panel != null && (syncWithIde.isSelected() != state.isSyncUiLanguageWithIde()
                || !uiLanguage.getSelectedItem().equals(state.getUiLanguage()));
    }

    void apply() {
        state.setSyncUiLanguageWithIde(syncWithIde.isSelected());
        state.setUiLanguage((LanguageDomain) uiLanguage.getSelectedItem());
    }

    void reset() {
        if (panel == null) {
            return;
        }
        syncWithIde.setSelected(state.isSyncUiLanguageWithIde());
        uiLanguage.setSelectedItem(state.getUiLanguage() == null ? LanguageDomain.EN_US : state.getUiLanguage());
        updateEnabledState();
        refreshTexts();
    }

    void refreshLanguage() {
        if (panel != null) {
            refreshTexts();
        }
    }

    private void refreshTexts() {
        ResourceBundle bundle = CommUtil.i18nResourceBundle(null);
        description.setText(bundle.getString("plugin.uiLanguage.description"));
        syncWithIde.setText(bundle.getString("plugin.uiLanguage.syncWithIde"));
        uiLanguageLabel.setText(bundle.getString("plugin.uiLanguage.select"));
    }

    private void updateEnabledState() {
        uiLanguage.setEnabled(!syncWithIde.isSelected());
    }
}
