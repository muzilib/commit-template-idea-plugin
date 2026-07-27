package com.c301.plugin.config;

import com.c301.plugin.domain.commit.CommitMessageRules;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/** Settings UI for validation and formatting rules, split by global defaults and project overrides. */
final class CommitMessageRulesConfigurable {
    private final ProjectCommitTemplateOverrideState projectState;
    private final StoreCommitTemplateState globalState = StoreCommitTemplateState.getInstance();

    private JTabbedPane root;
    private JCheckBox globalRequireType;
    private JCheckBox globalRequireScope;
    private JSpinner globalSubjectLength;
    private JSpinner globalWrapLength;
    private JTextField globalIssueKeyword;
    private JCheckBox globalForbidPeriod;

    private JCheckBox overrideRequireType;
    private JCheckBox projectRequireType;
    private JCheckBox overrideRequireScope;
    private JCheckBox projectRequireScope;
    private JCheckBox overrideSubjectLength;
    private JSpinner projectSubjectLength;
    private JCheckBox overrideWrapLength;
    private JSpinner projectWrapLength;
    private JCheckBox overrideIssueKeyword;
    private JTextField projectIssueKeyword;
    private JCheckBox overrideForbidPeriod;
    private JCheckBox projectForbidPeriod;

    CommitMessageRulesConfigurable(ProjectCommitTemplateOverrideState projectState) {
        this.projectState = projectState;
    }

    JComponent createComponent() {
        if (root == null) {
            root = new JTabbedPane();
            root.addTab("Global Defaults", createGlobalPanel());
            root.addTab("Project Overrides", createProjectPanel());
        }
        reset();
        return root;
    }

    boolean isModified() {
        if (root == null) {
            return false;
        }
        CommitMessageRules global = effectiveGlobalRules();
        return globalRequireType.isSelected() != global.requireCommitType()
                || globalRequireScope.isSelected() != global.requireScope()
                || (Integer) globalSubjectLength.getValue() != global.subjectMaxLength()
                || (Integer) globalWrapLength.getValue() != global.bodyWrapLength()
                || !globalIssueKeyword.getText().trim().equals(global.issueFooterKeyword())
                || globalForbidPeriod.isSelected() != global.forbidSubjectTrailingPeriod()
                || overrideRequireType.isSelected() == (projectState.getRequireCommitType() == null)
                || (overrideRequireType.isSelected() && projectRequireType.isSelected() != projectState.getRequireCommitType())
                || overrideRequireScope.isSelected() == (projectState.getRequireScope() == null)
                || (overrideRequireScope.isSelected() && projectRequireScope.isSelected() != projectState.getRequireScope())
                || overrideSubjectLength.isSelected() == (projectState.getSubjectMaxLength() == null)
                || (overrideSubjectLength.isSelected() && projectSubjectLength.getValue() != projectState.getSubjectMaxLength())
                || overrideWrapLength.isSelected() == (projectState.getBodyWrapLength() == null)
                || (overrideWrapLength.isSelected() && projectWrapLength.getValue() != projectState.getBodyWrapLength())
                || overrideIssueKeyword.isSelected() == (projectState.getIssueFooterKeyword() == null)
                || (overrideIssueKeyword.isSelected() && !projectIssueKeyword.getText().trim().equals(projectState.getIssueFooterKeyword()))
                || overrideForbidPeriod.isSelected() == (projectState.getForbidSubjectTrailingPeriod() == null)
                || (overrideForbidPeriod.isSelected() && projectForbidPeriod.isSelected() != projectState.getForbidSubjectTrailingPeriod());
    }

    void apply() throws ConfigurationException {
        String keyword = globalIssueKeyword.getText().trim();
        if (keyword.isEmpty()) {
            throw new ConfigurationException("Issue footer keyword cannot be empty.");
        }
        globalState.setCommitMessageRules(CommitMessageRulesState.fromDomain(new CommitMessageRules(
                globalRequireType.isSelected(), globalRequireScope.isSelected(),
                (Integer) globalSubjectLength.getValue(), (Integer) globalWrapLength.getValue(),
                keyword, globalForbidPeriod.isSelected())));

        if (overrideIssueKeyword.isSelected() && projectIssueKeyword.getText().trim().isEmpty()) {
            throw new ConfigurationException("Project issue footer keyword cannot be empty when overridden.");
        }
        projectState.setRequireCommitType(overrideRequireType.isSelected() ? projectRequireType.isSelected() : null);
        projectState.setRequireScope(overrideRequireScope.isSelected() ? projectRequireScope.isSelected() : null);
        projectState.setSubjectMaxLength(overrideSubjectLength.isSelected() ? (Integer) projectSubjectLength.getValue() : null);
        projectState.setBodyWrapLength(overrideWrapLength.isSelected() ? (Integer) projectWrapLength.getValue() : null);
        projectState.setIssueFooterKeyword(overrideIssueKeyword.isSelected() ? projectIssueKeyword.getText().trim() : null);
        projectState.setForbidSubjectTrailingPeriod(overrideForbidPeriod.isSelected() ? projectForbidPeriod.isSelected() : null);
    }

    void reset() {
        if (root == null) {
            return;
        }
        CommitMessageRules global = effectiveGlobalRules();
        globalRequireType.setSelected(global.requireCommitType());
        globalRequireScope.setSelected(global.requireScope());
        globalSubjectLength.setValue(global.subjectMaxLength());
        globalWrapLength.setValue(global.bodyWrapLength());
        globalIssueKeyword.setText(global.issueFooterKeyword());
        globalForbidPeriod.setSelected(global.forbidSubjectTrailingPeriod());

        overrideRequireType.setSelected(projectState.getRequireCommitType() != null);
        projectRequireType.setSelected(projectState.getRequireCommitType() == null ? global.requireCommitType() : projectState.getRequireCommitType());
        overrideRequireScope.setSelected(projectState.getRequireScope() != null);
        projectRequireScope.setSelected(projectState.getRequireScope() == null ? global.requireScope() : projectState.getRequireScope());
        overrideSubjectLength.setSelected(projectState.getSubjectMaxLength() != null);
        projectSubjectLength.setValue(projectState.getSubjectMaxLength() == null ? global.subjectMaxLength() : projectState.getSubjectMaxLength());
        overrideWrapLength.setSelected(projectState.getBodyWrapLength() != null);
        projectWrapLength.setValue(projectState.getBodyWrapLength() == null ? global.bodyWrapLength() : projectState.getBodyWrapLength());
        overrideIssueKeyword.setSelected(projectState.getIssueFooterKeyword() != null);
        projectIssueKeyword.setText(projectState.getIssueFooterKeyword() == null ? global.issueFooterKeyword() : projectState.getIssueFooterKeyword());
        overrideForbidPeriod.setSelected(projectState.getForbidSubjectTrailingPeriod() != null);
        projectForbidPeriod.setSelected(projectState.getForbidSubjectTrailingPeriod() == null
                ? global.forbidSubjectTrailingPeriod() : projectState.getForbidSubjectTrailingPeriod());
        updateProjectEnabledState();
    }

    private JPanel createGlobalPanel() {
        JPanel panel = createPanel();
        GridBagConstraints constraints = constraints(0);
        globalRequireType = new JCheckBox("Require commit type");
        panel.add(globalRequireType, constraints);
        constraints.gridy++;
        globalRequireScope = new JCheckBox("Require scope");
        panel.add(globalRequireScope, constraints);
        constraints.gridy++;
        panel.add(new JLabel("Subject maximum length"), constraints);
        constraints.gridy++;
        globalSubjectLength = spinner(CommitMessageRules.DEFAULT_SUBJECT_MAX_LENGTH);
        panel.add(globalSubjectLength, constraints);
        constraints.gridy++;
        panel.add(new JLabel("Body wrap length"), constraints);
        constraints.gridy++;
        globalWrapLength = spinner(CommitMessageRules.DEFAULT_BODY_WRAP_LENGTH);
        panel.add(globalWrapLength, constraints);
        constraints.gridy++;
        panel.add(new JLabel("Issue footer keyword"), constraints);
        constraints.gridy++;
        globalIssueKeyword = new JTextField();
        panel.add(globalIssueKeyword, constraints);
        constraints.gridy++;
        globalForbidPeriod = new JCheckBox("Disallow a trailing period in subject");
        panel.add(globalForbidPeriod, constraints);
        return panel;
    }

    private JPanel createProjectPanel() {
        JPanel panel = createPanel();
        GridBagConstraints constraints = constraints(0);
        panel.add(new JLabel("Unchecked rules inherit the global default."), constraints);
        constraints.gridy++;
        overrideRequireType = new JCheckBox("Override required commit type");
        panel.add(overrideRequireType, constraints);
        constraints.gridy++;
        projectRequireType = new JCheckBox("Require commit type");
        panel.add(projectRequireType, indented(constraints));
        constraints.gridy++;
        overrideRequireScope = new JCheckBox("Override required scope");
        panel.add(overrideRequireScope, constraints);
        constraints.gridy++;
        projectRequireScope = new JCheckBox("Require scope");
        panel.add(projectRequireScope, indented(constraints));
        constraints.gridy++;
        overrideSubjectLength = new JCheckBox("Override subject maximum length");
        panel.add(overrideSubjectLength, constraints);
        constraints.gridy++;
        projectSubjectLength = spinner(CommitMessageRules.DEFAULT_SUBJECT_MAX_LENGTH);
        panel.add(projectSubjectLength, indented(constraints));
        constraints.gridy++;
        overrideWrapLength = new JCheckBox("Override body wrap length");
        panel.add(overrideWrapLength, constraints);
        constraints.gridy++;
        projectWrapLength = spinner(CommitMessageRules.DEFAULT_BODY_WRAP_LENGTH);
        panel.add(projectWrapLength, indented(constraints));
        constraints.gridy++;
        overrideIssueKeyword = new JCheckBox("Override issue footer keyword");
        panel.add(overrideIssueKeyword, constraints);
        constraints.gridy++;
        projectIssueKeyword = new JTextField();
        panel.add(projectIssueKeyword, indented(constraints));
        constraints.gridy++;
        overrideForbidPeriod = new JCheckBox("Override trailing-period rule");
        panel.add(overrideForbidPeriod, constraints);
        constraints.gridy++;
        projectForbidPeriod = new JCheckBox("Disallow a trailing period in subject");
        panel.add(projectForbidPeriod, indented(constraints));

        overrideRequireType.addActionListener(e -> updateProjectEnabledState());
        overrideRequireScope.addActionListener(e -> updateProjectEnabledState());
        overrideSubjectLength.addActionListener(e -> updateProjectEnabledState());
        overrideWrapLength.addActionListener(e -> updateProjectEnabledState());
        overrideIssueKeyword.addActionListener(e -> updateProjectEnabledState());
        overrideForbidPeriod.addActionListener(e -> updateProjectEnabledState());
        return panel;
    }

    private void updateProjectEnabledState() {
        projectRequireType.setEnabled(overrideRequireType.isSelected());
        projectRequireScope.setEnabled(overrideRequireScope.isSelected());
        projectSubjectLength.setEnabled(overrideSubjectLength.isSelected());
        projectWrapLength.setEnabled(overrideWrapLength.isSelected());
        projectIssueKeyword.setEnabled(overrideIssueKeyword.isSelected());
        projectForbidPeriod.setEnabled(overrideForbidPeriod.isSelected());
    }

    private CommitMessageRules effectiveGlobalRules() {
        return globalState.getCommitMessageRules() == null
                ? CommitMessageRules.defaults()
                : globalState.getCommitMessageRules().toDomain();
    }

    private static JSpinner spinner(int value) {
        return new JSpinner(new SpinnerNumberModel(value, 1, 500, 1));
    }

    private static JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return panel;
    }

    private static GridBagConstraints constraints(int y) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = y;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = JBUI.insets(4, 0);
        return constraints;
    }

    private static GridBagConstraints indented(GridBagConstraints source) {
        GridBagConstraints copy = (GridBagConstraints) source.clone();
        copy.insets = JBUI.insets(2, 24, 4, 0);
        return copy;
    }
}
