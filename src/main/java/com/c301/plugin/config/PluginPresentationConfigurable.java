package com.c301.plugin.config;

import com.c301.plugin.model.GitmojiLocationDomain;
import com.c301.plugin.utils.CommUtil;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * 控制可选提交信息展示效果的全局偏好设置。
 */
final class PluginPresentationConfigurable {
    private final StoreCommitTemplateState state = StoreCommitTemplateState.getInstance();
    private JPanel panel;
    private JCheckBox showPreview;
    private JCheckBox enableGitmoji;
    private JPanel gitmojiOptions;
    private JLabel gitmojiLocationLabel;
    private JRadioButton location1;
    private JRadioButton location2;
    private JRadioButton location3;

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
            panel.setBorder(JBUI.Borders.emptyTop(8));
            GridBagConstraints constraints = constraints(0);

            showPreview = new JCheckBox();
            panel.add(showPreview, constraints);
            constraints.gridy++;
            enableGitmoji = new JCheckBox();
            panel.add(enableGitmoji, constraints);

            constraints.gridy++;
            gitmojiOptions = new JPanel(new GridBagLayout());
            GridBagConstraints optionConstraints = constraints(0);
            optionConstraints.insets = JBUI.insets(8, 24, 2, 0);
            gitmojiLocationLabel = new JLabel();
            gitmojiOptions.add(gitmojiLocationLabel, optionConstraints);
            optionConstraints.gridy++;
            location1 = new JRadioButton();
            gitmojiOptions.add(location1, optionConstraints);
            optionConstraints.gridy++;
            location2 = new JRadioButton();
            gitmojiOptions.add(location2, optionConstraints);
            optionConstraints.gridy++;
            location3 = new JRadioButton();
            gitmojiOptions.add(location3, optionConstraints);
            ButtonGroup locations = new ButtonGroup();
            locations.add(location1);
            locations.add(location2);
            locations.add(location3);
            panel.add(gitmojiOptions, constraints);

            enableGitmoji.addActionListener(event -> updateGitmojiOptionsVisibility());
            refreshLanguage();
        }
        reset();
        return panel;
    }

    boolean isModified() {
        return panel != null && (showPreview.isSelected() != state.isPreviewEnabled()
                || enableGitmoji.isSelected() != state.isEmojiEnable()
                || (enableGitmoji.isSelected() && selectedLocation() != state.getEmojiLocation()));
    }

    void apply() {
        state.setPreviewEnabled(showPreview.isSelected());
        state.setEmojiEnable(enableGitmoji.isSelected());
        state.setEmojiLocation(selectedLocation());
    }

    void reset() {
        if (panel == null) {
            return;
        }
        showPreview.setSelected(state.isPreviewEnabled());
        enableGitmoji.setSelected(state.isEmojiEnable());
        selectLocation(state.getEmojiLocation());
        updateGitmojiOptionsVisibility();
    }

    void refreshLanguage() {
        if (panel == null) {
            return;
        }
        ResourceBundle bundle = CommUtil.i18nResourceBundle(null);
        showPreview.setText(bundle.getString("plugin.preferences.showPreview"));
        enableGitmoji.setText(bundle.getString("plugin.preferences.enableGitmoji"));
        gitmojiLocationLabel.setText(bundle.getString("plugin.preferences.gitmojiLocation"));
        location1.setText(bundle.getString("plugin.setting.location.location1"));
        location2.setText(bundle.getString("plugin.setting.location.location2"));
        location3.setText(bundle.getString("plugin.setting.location.location3"));
    }

    private void updateGitmojiOptionsVisibility() {
        gitmojiOptions.setVisible(enableGitmoji.isSelected());
        panel.revalidate();
        panel.repaint();
    }

    private GitmojiLocationDomain selectedLocation() {
        if (location2.isSelected()) {
            return GitmojiLocationDomain.LOCATION2;
        }
        if (location3.isSelected()) {
            return GitmojiLocationDomain.LOCATION3;
        }
        return GitmojiLocationDomain.LOCATION1;
    }

    private void selectLocation(GitmojiLocationDomain location) {
        if (GitmojiLocationDomain.LOCATION2.equals(location)) {
            location2.setSelected(true);
        } else if (GitmojiLocationDomain.LOCATION3.equals(location)) {
            location3.setSelected(true);
        } else {
            location1.setSelected(true);
        }
    }
}
