package com.c301.plugin.config;

import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.TitledSeparator;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * 承载与提交模板内容无关的全局插件偏好设置。
 * 后续新增偏好分区应放在这里，以保持顶层设置导航结构稳定。
 */
final class PluginPreferencesConfigurable {
    private final PluginUiLanguageConfigurable uiLanguageConfigurable = new PluginUiLanguageConfigurable();
    private final PluginPresentationConfigurable presentationConfigurable = new PluginPresentationConfigurable();
    private final AiPreferencesConfigurable aiConfigurable = new AiPreferencesConfigurable();
    private JPanel panel;
    private JScrollPane scrollPane;
    private TitledSeparator presentationSection;
    private TitledSeparator uiLanguageSection;
    private TitledSeparator aiSection;

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
            constraints.insets = JBUI.insetsTop(16);
            aiSection = new TitledSeparator();
            panel.add(aiSection, constraints);

            constraints.gridy++;
            constraints.insets = JBUI.emptyInsets();
            panel.add(aiConfigurable.createComponent(), constraints);

            constraints.gridy++;
            constraints.weighty = 1;
            constraints.fill = GridBagConstraints.VERTICAL;
            panel.add(Box.createVerticalGlue(), constraints);

            scrollPane = new JScrollPane(panel,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(JBUI.Borders.empty());
            scrollPane.getVerticalScrollBar().setUnitIncrement(JBUI.scale(16));
            refreshLanguage();
        }
        return scrollPane;
    }

    boolean isModified() {
        return presentationConfigurable.isModified() || uiLanguageConfigurable.isModified() || aiConfigurable.isModified();
    }

    void apply() throws ConfigurationException {
        presentationConfigurable.apply();
        uiLanguageConfigurable.apply();
        aiConfigurable.apply();
    }

    void reset() {
        presentationConfigurable.reset();
        uiLanguageConfigurable.reset();
        aiConfigurable.reset();
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
        if (aiSection != null) {
            aiSection.setText("AI 提交建议");
        }
    }
}
