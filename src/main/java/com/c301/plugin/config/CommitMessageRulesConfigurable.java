package com.c301.plugin.config;

import com.c301.plugin.domain.commit.CommitMessageRules;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/** Global validation and formatting settings shared by every project. */
final class CommitMessageRulesConfigurable {
    private final StoreCommitTemplateState globalState = StoreCommitTemplateState.getInstance();

    private JPanel panel;
    private JLabel description;
    private JCheckBox requireType;
    private JCheckBox requireScope;
    private JSpinner subjectLength;
    private JSpinner wrapLength;
    private JTextField issueKeyword;
    private JCheckBox forbidPeriod;
    private JCheckBox previewEnabled;
    private JLabel subjectLengthLabel;
    private JLabel wrapLengthLabel;
    private JLabel issueKeywordLabel;

    JComponent createComponent() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            GridBagConstraints constraints = constraints(0);

            description = new JLabel(text("plugin.rules.description"));
            description.setForeground(UIManager.getColor("Label.disabledForeground"));
            panel.add(description, constraints);
            constraints.gridy++;
            requireType = new JCheckBox(text("plugin.rules.requireType"));
            panel.add(requireType, constraints);
            constraints.gridy++;
            requireScope = new JCheckBox(text("plugin.rules.requireScope"));
            panel.add(requireScope, constraints);
            constraints.gridy++;
            subjectLengthLabel = new JLabel(text("plugin.rules.subjectLength"));
            panel.add(subjectLengthLabel, constraints);
            constraints.gridy++;
            subjectLength = spinner(CommitMessageRules.DEFAULT_SUBJECT_MAX_LENGTH);
            panel.add(subjectLength, constraints);
            constraints.gridy++;
            wrapLengthLabel = new JLabel(text("plugin.rules.bodyWrapLength"));
            panel.add(wrapLengthLabel, constraints);
            constraints.gridy++;
            wrapLength = spinner(CommitMessageRules.DEFAULT_BODY_WRAP_LENGTH);
            panel.add(wrapLength, constraints);
            constraints.gridy++;
            issueKeywordLabel = new JLabel(text("plugin.rules.issueFooterKeyword"));
            panel.add(issueKeywordLabel, constraints);
            constraints.gridy++;
            issueKeyword = new JTextField();
            panel.add(issueKeyword, constraints);
            constraints.gridy++;
            forbidPeriod = new JCheckBox(text("plugin.rules.forbidTrailingPeriod"));
            panel.add(forbidPeriod, constraints);
            constraints.gridy++;
            previewEnabled = new JCheckBox(text("plugin.rules.showPreview"));
            panel.add(previewEnabled, constraints);

            GridBagConstraints filler = constraints(constraints.gridy + 1);
            filler.weighty = 1;
            filler.fill = GridBagConstraints.VERTICAL;
            panel.add(Box.createVerticalGlue(), filler);
        }
        reset();
        return panel;
    }

    boolean isModified() {
        if (panel == null) {
            return false;
        }
        CommitMessageRules rules = effectiveRules();
        return requireType.isSelected() != rules.requireCommitType()
                || requireScope.isSelected() != rules.requireScope()
                || (Integer) subjectLength.getValue() != rules.subjectMaxLength()
                || (Integer) wrapLength.getValue() != rules.bodyWrapLength()
                || !issueKeyword.getText().trim().equals(rules.issueFooterKeyword())
                || forbidPeriod.isSelected() != rules.forbidSubjectTrailingPeriod()
                || previewEnabled.isSelected() != globalState.isPreviewEnabled();
    }

    void apply() throws ConfigurationException {
        String keyword = issueKeyword.getText().trim();
        if (keyword.isEmpty()) {
            throw new ConfigurationException(text("plugin.rules.error.issueKeywordRequired"));
        }
        globalState.setPreviewEnabled(previewEnabled.isSelected());
        globalState.setCommitMessageRules(CommitMessageRulesState.fromDomain(new CommitMessageRules(
                requireType.isSelected(), requireScope.isSelected(),
                (Integer) subjectLength.getValue(), (Integer) wrapLength.getValue(),
                keyword, forbidPeriod.isSelected())));
    }

    void refreshLanguage() {
        if (panel == null) {
            return;
        }
        description.setText(text("plugin.rules.description"));
        requireType.setText(text("plugin.rules.requireType"));
        requireScope.setText(text("plugin.rules.requireScope"));
        subjectLengthLabel.setText(text("plugin.rules.subjectLength"));
        wrapLengthLabel.setText(text("plugin.rules.bodyWrapLength"));
        issueKeywordLabel.setText(text("plugin.rules.issueFooterKeyword"));
        forbidPeriod.setText(text("plugin.rules.forbidTrailingPeriod"));
        previewEnabled.setText(text("plugin.rules.showPreview"));
        panel.revalidate();
        panel.repaint();
    }

    void reset() {
        if (panel == null) {
            return;
        }
        CommitMessageRules rules = effectiveRules();
        requireType.setSelected(rules.requireCommitType());
        requireScope.setSelected(rules.requireScope());
        subjectLength.setValue(rules.subjectMaxLength());
        wrapLength.setValue(rules.bodyWrapLength());
        issueKeyword.setText(rules.issueFooterKeyword());
        forbidPeriod.setSelected(rules.forbidSubjectTrailingPeriod());
        previewEnabled.setSelected(globalState.isPreviewEnabled());
    }

    private CommitMessageRules effectiveRules() {
        return globalState.getCommitMessageRules() == null
                ? CommitMessageRules.defaults()
                : globalState.getCommitMessageRules().toDomain();
    }

    private static JSpinner spinner(int value) {
        return new JSpinner(new SpinnerNumberModel(value, 1, 500, 1));
    }

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

    private static String text(String key) {
        return CommUtil.i18nResourceBundle(null).getString(key);
    }
}
