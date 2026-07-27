package com.c301.plugin.config;

import com.c301.plugin.utils.CommUtil;
import com.intellij.ui.TitledSeparator;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Container for global plugin preferences that are independent of commit-template content.
 * Add future preference sections here to keep the top-level settings navigation stable.
 */
final class PluginPreferencesConfigurable {
    private final PluginUiLanguageConfigurable uiLanguageConfigurable = new PluginUiLanguageConfigurable();
    private final PluginPresentationConfigurable presentationConfigurable = new PluginPresentationConfigurable();
    private JPanel panel;
    private TitledSeparator presentationSection;
    private TitledSeparator uiLanguageSection;

    JComponent createComponent() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            panel.setBorder(JBUI.Borders.empty(12));

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 1;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            presentationSection = new TitledSeparator();
            panel.add(presentationSection, constraints);

            constraints.gridy++;
            panel.add(presentationConfigurable.createComponent(), constraints);

            constraints.gridy++;
            constraints.insets = JBUI.insetsTop(16);
            uiLanguageSection = new TitledSeparator();
            panel.add(uiLanguageSection, constraints);

            constraints.gridy++;
            constraints.insets = JBUI.emptyInsets();
            panel.add(uiLanguageConfigurable.createComponent(), constraints);

            constraints.gridy++;
            constraints.weighty = 1;
            constraints.fill = GridBagConstraints.VERTICAL;
            panel.add(Box.createVerticalGlue(), constraints);
            refreshLanguage();
        }
        return panel;
    }

    boolean isModified() {
        return presentationConfigurable.isModified() || uiLanguageConfigurable.isModified();
    }

    void apply() {
        presentationConfigurable.apply();
        uiLanguageConfigurable.apply();
    }

    void reset() {
        presentationConfigurable.reset();
        uiLanguageConfigurable.reset();
    }

    void refreshLanguage() {
        presentationConfigurable.refreshLanguage();
        uiLanguageConfigurable.refreshLanguage();
        ResourceBundle bundle = CommUtil.i18nResourceBundle(null);
        if (presentationSection != null) {
            presentationSection.setText(bundle.getString("plugin.preferences.section.presentation"));
        }
        if (uiLanguageSection != null) {
            uiLanguageSection.setText(bundle.getString("plugin.preferences.section.uiLanguage"));
        }
    }
}
