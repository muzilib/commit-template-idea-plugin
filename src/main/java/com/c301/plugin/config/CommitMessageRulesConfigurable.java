package com.c301.plugin.config;

import com.c301.plugin.domain.commit.CommitMessageRules;
import com.c301.plugin.utils.CommUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import java.awt.*;

/**
 * 所有项目共用的全局提交信息校验与格式化设置。
 */
final class CommitMessageRulesConfigurable {
    private final StoreCommitTemplateState globalState = StoreCommitTemplateState.getInstance();

    private JPanel panel;
    private JLabel description;
    private JCheckBox requireType;
    private JCheckBox requireScope;
    private JCheckBox subjectLengthLimitEnabled;
    private JCheckBox wrapTextByDefault;
    private JTextField subjectLength;
    private JTextField wrapLength;
    private JTextField issueKeyword;
    private JCheckBox forbidPeriod;
    private JLabel subjectLengthLabel;
    private JLabel wrapLengthLabel;
    private JLabel issueKeywordLabel;

    private static JTextField numericField(int value) {
        JTextField field = new JTextField(String.valueOf(value));
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass bypass, int offset, String text, javax.swing.text.AttributeSet attributes)
                    throws javax.swing.text.BadLocationException {
                if (text != null && text.chars().allMatch(Character::isDigit)
                        && bypass.getDocument().getLength() + text.length() <= 4) {
                    super.insertString(bypass, offset, text, attributes);
                }
            }

            @Override
            public void replace(FilterBypass bypass, int offset, int length, String text,
                                javax.swing.text.AttributeSet attributes) throws javax.swing.text.BadLocationException {
                if (text != null && text.chars().allMatch(Character::isDigit)
                        && bypass.getDocument().getLength() - length + text.length() <= 4) {
                    super.replace(bypass, offset, length, text, attributes);
                }
            }
        });
        return field;
    }

    private static int readNumericField(JTextField field, String fieldName) throws ConfigurationException {
        String value = field.getText().trim();
        try {
            int number = Integer.parseInt(value);
            if (number < 0 || number > 9999) {
                throw new NumberFormatException();
            }
            return number;
        } catch (NumberFormatException exception) {
            throw new ConfigurationException(text("plugin.rules.error.numericRange").replace("{field}", fieldName));
        }
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
            subjectLengthLimitEnabled = new JCheckBox(text("plugin.rules.subjectLengthLimitEnabled"));
            panel.add(subjectLengthLimitEnabled, constraints);
            constraints.gridy++;
            subjectLengthLabel = new JLabel(text("plugin.rules.subjectLength"));
            panel.add(subjectLengthLabel, constraints);
            constraints.gridy++;
            subjectLength = numericField(CommitMessageRules.DEFAULT_SUBJECT_MAX_LENGTH);
            subjectLength.setPreferredSize(JBUI.size(120, subjectLength.getPreferredSize().height));
            subjectLength.setMaximumSize(JBUI.size(120, subjectLength.getPreferredSize().height));
            panel.add(subjectLength, constraints);
            subjectLengthLimitEnabled.addActionListener(event -> updateSubjectLengthEnabled());
            constraints.gridy++;
            wrapTextByDefault = new JCheckBox();
            panel.add(wrapTextByDefault, constraints);
            constraints.gridy++;
            wrapLengthLabel = new JLabel(text("plugin.rules.bodyWrapLength"));
            panel.add(wrapLengthLabel, constraints);
            constraints.gridy++;
            wrapLength = numericField(CommitMessageRules.DEFAULT_BODY_WRAP_LENGTH);
            wrapLength.setPreferredSize(JBUI.size(120, wrapLength.getPreferredSize().height));
            wrapLength.setMaximumSize(JBUI.size(120, wrapLength.getPreferredSize().height));
            panel.add(wrapLength, constraints);
            wrapLength.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent event) {
                    refreshWrapTextLabel();
                }

                public void removeUpdate(DocumentEvent event) {
                    refreshWrapTextLabel();
                }

                public void changedUpdate(DocumentEvent event) {
                    refreshWrapTextLabel();
                }
            });
            constraints.gridy++;
            issueKeywordLabel = new JLabel(text("plugin.rules.issueFooterKeyword"));
            panel.add(issueKeywordLabel, constraints);
            constraints.gridy++;
            issueKeyword = new JTextField();
            issueKeyword.setColumns(20);
            issueKeyword.setPreferredSize(JBUI.size(260, issueKeyword.getPreferredSize().height));
            issueKeyword.setMaximumSize(JBUI.size(260, issueKeyword.getPreferredSize().height));
            panel.add(issueKeyword, constraints);
            constraints.gridy++;
            forbidPeriod = new JCheckBox(text("plugin.rules.forbidTrailingPeriod"));
            panel.add(forbidPeriod, constraints);

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
                || subjectLengthLimitEnabled.isSelected() != rules.subjectLengthLimitEnabled()
                || !subjectLength.getText().trim().equals(String.valueOf(rules.subjectMaxLength()))
                || !wrapLength.getText().trim().equals(String.valueOf(rules.bodyWrapLength()))
                || wrapTextByDefault.isSelected() != rules.wrapTextByDefault()
                || !issueKeyword.getText().trim().equals(rules.issueFooterKeyword())
                || forbidPeriod.isSelected() != rules.forbidSubjectTrailingPeriod();
    }

    void apply() throws ConfigurationException {
        String keyword = issueKeyword.getText().trim();
        int subjectLengthValue = readNumericField(subjectLength, text("plugin.rules.subjectLength"));
        int wrapLengthValue = readNumericField(wrapLength, text("plugin.rules.bodyWrapLength"));
        if (keyword.isEmpty()) {
            throw new ConfigurationException(text("plugin.rules.error.issueKeywordRequired"));
        }
        globalState.setCommitMessageRules(CommitMessageRulesState.fromDomain(new CommitMessageRules(
                requireType.isSelected(), requireScope.isSelected(),
                subjectLengthValue, wrapLengthValue,
                keyword, forbidPeriod.isSelected(), subjectLengthLimitEnabled.isSelected(),
                wrapTextByDefault.isSelected())));
    }

    void refreshLanguage() {
        if (panel == null) {
            return;
        }
        description.setText(text("plugin.rules.description"));
        requireType.setText(text("plugin.rules.requireType"));
        requireScope.setText(text("plugin.rules.requireScope"));
        subjectLengthLimitEnabled.setText(text("plugin.rules.subjectLengthLimitEnabled"));
        subjectLengthLabel.setText(text("plugin.rules.subjectLength"));
        wrapLengthLabel.setText(text("plugin.rules.bodyWrapLength"));
        wrapTextByDefault.setText(text("plugin.rules.wrapTextByDefault").replace("{length}", wrapLength.getText()));
        issueKeywordLabel.setText(text("plugin.rules.issueFooterKeyword"));
        forbidPeriod.setText(text("plugin.rules.forbidTrailingPeriod"));
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
        subjectLengthLimitEnabled.setSelected(rules.subjectLengthLimitEnabled());
        subjectLength.setText(String.valueOf(rules.subjectMaxLength()));
        updateSubjectLengthEnabled();
        wrapLength.setText(String.valueOf(rules.bodyWrapLength()));
        wrapTextByDefault.setSelected(rules.wrapTextByDefault());
        wrapTextByDefault.setText(text("plugin.rules.wrapTextByDefault").replace("{length}", wrapLength.getText()));
        issueKeyword.setText(rules.issueFooterKeyword());
        forbidPeriod.setSelected(rules.forbidSubjectTrailingPeriod());
    }

    private void updateSubjectLengthEnabled() {
        boolean enabled = subjectLengthLimitEnabled.isSelected();
        subjectLengthLabel.setEnabled(enabled);
        subjectLength.setEnabled(enabled);
        wrapLength.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                refreshWrapTextLabel();
            }

            public void removeUpdate(DocumentEvent event) {
                refreshWrapTextLabel();
            }

            public void changedUpdate(DocumentEvent event) {
                refreshWrapTextLabel();
            }
        });
    }

    private void refreshWrapTextLabel() {
        if (wrapTextByDefault != null) {
            wrapTextByDefault.setText(text("plugin.rules.wrapTextByDefault").replace("{length}", wrapLength.getText()));
        }
    }

    private CommitMessageRules effectiveRules() {
        return globalState.getCommitMessageRules() == null
                ? CommitMessageRules.defaults()
                : globalState.getCommitMessageRules().toDomain();
    }
}
