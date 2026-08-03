package com.c301.plugin.config;

import com.c301.plugin.domain.commit.CommitMessageRules;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 可序列化为 XML 的全局提交信息规则默认配置。
 */
@Data
@NoArgsConstructor
public class CommitMessageRulesState {
    private boolean requireCommitType = true;
    private boolean requireScope = false;
    private int subjectMaxLength = CommitMessageRules.DEFAULT_SUBJECT_MAX_LENGTH;
    private int bodyWrapLength = CommitMessageRules.DEFAULT_BODY_WRAP_LENGTH;
    private String issueFooterKeyword = CommitMessageRules.DEFAULT_ISSUE_FOOTER_KEYWORD;
    private boolean forbidSubjectTrailingPeriod = false;
    private boolean subjectLengthLimitEnabled = false;
    private boolean wrapTextByDefault = false;

    public static CommitMessageRulesState fromDomain(CommitMessageRules rules) {
        var state = new CommitMessageRulesState();
        state.setRequireCommitType(rules.requireCommitType());
        state.setRequireScope(rules.requireScope());
        state.setSubjectMaxLength(rules.subjectMaxLength());
        state.setBodyWrapLength(rules.bodyWrapLength());
        state.setIssueFooterKeyword(rules.issueFooterKeyword());
        state.setForbidSubjectTrailingPeriod(rules.forbidSubjectTrailingPeriod());
        state.setSubjectLengthLimitEnabled(rules.subjectLengthLimitEnabled());
        state.setWrapTextByDefault(rules.wrapTextByDefault());
        return state;
    }

    public CommitMessageRules toDomain() {
        return new CommitMessageRules(requireCommitType, requireScope, subjectMaxLength,
                bodyWrapLength, issueFooterKeyword, forbidSubjectTrailingPeriod, subjectLengthLimitEnabled,
                wrapTextByDefault);
    }
}
