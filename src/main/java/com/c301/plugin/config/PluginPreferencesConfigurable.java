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
    private final Runnable resetAllAction;
    private final Runnable showAnnouncementAction;
    private JPanel panel;
    private TitledSeparator presentationSection;
    private TitledSeparator uiLanguageSection;
    private TitledSeparator advancedSection;
    private JButton resetAllButton;
    private JButton showAnnouncementButton;

    PluginPreferencesConfigurable(Runnable resetAllAction, Runnable showAnnouncementAction) {
        this.resetAllAction = resetAllAction;
        this.showAnnouncementAction = showAnnouncementAction;
    }

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
            advancedSection = new TitledSeparator();
            panel.add(advancedSection, constraints);

            constraints.gridy++;
            constraints.insets = JBUI.emptyInsets();
            JPanel advancedActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            resetAllButton = new JButton();
            resetAllButton.addActionListener(event -> resetAllAction.run());
            advancedActions.add(resetAllButton);
            showAnnouncementButton = new JButton();
            showAnnouncementButton.addActionListener(event -> showAnnouncementAction.run());
            advancedActions.add(Box.createHorizontalStrut(JBUI.scale(8)));
            advancedActions.add(showAnnouncementButton);
            panel.add(advancedActions, constraints);

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

    void apply() throws ConfigurationException {
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
        if (advancedSection != null) {
            advancedSection.setText(bundle.getString("plugin.preferences.section.advanced"));
        }
        if (resetAllButton != null) {
            resetAllButton.setText(bundle.getString("plugin.preferences.resetAll"));
        }
        if (showAnnouncementButton != null) {
            showAnnouncementButton.setText(bundle.getString("plugin.preferences.showAnnouncement"));
        }
    }
}
