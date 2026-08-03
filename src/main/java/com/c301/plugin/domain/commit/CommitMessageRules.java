package com.c301.plugin.domain.commit;

/**
 * 控制提交信息校验与格式化的非敏感规则。
 */
public record CommitMessageRules(
        boolean requireCommitType,
        boolean requireScope,
        int subjectMaxLength,
        int bodyWrapLength,
        String issueFooterKeyword,
        boolean forbidSubjectTrailingPeriod,
        boolean subjectLengthLimitEnabled,
        boolean wrapTextByDefault
) {
    public static final int DEFAULT_SUBJECT_MAX_LENGTH = 50;
    public static final int DEFAULT_BODY_WRAP_LENGTH = 72;
    public static final String DEFAULT_ISSUE_FOOTER_KEYWORD = "Closes";

    public CommitMessageRules(boolean requireCommitType, boolean requireScope, int subjectMaxLength,
                              int bodyWrapLength, String issueFooterKeyword,
                              boolean forbidSubjectTrailingPeriod) {
        this(requireCommitType, requireScope, subjectMaxLength, bodyWrapLength, issueFooterKeyword,
                forbidSubjectTrailingPeriod, true, false);
    }

    public CommitMessageRules(boolean requireCommitType, boolean requireScope, int subjectMaxLength,
                              int bodyWrapLength, String issueFooterKeyword,
                              boolean forbidSubjectTrailingPeriod, boolean subjectLengthLimitEnabled) {
        this(requireCommitType, requireScope, subjectMaxLength, bodyWrapLength, issueFooterKeyword,
                forbidSubjectTrailingPeriod, subjectLengthLimitEnabled, false);
    }

    public CommitMessageRules {
        subjectMaxLength = Math.max(0, subjectMaxLength);
        bodyWrapLength = Math.max(0, bodyWrapLength);
        issueFooterKeyword = issueFooterKeyword == null || issueFooterKeyword.isBlank()
                ? DEFAULT_ISSUE_FOOTER_KEYWORD
                : issueFooterKeyword.trim();
    }

    public static CommitMessageRules defaults() {
        return new CommitMessageRules(true, false, DEFAULT_SUBJECT_MAX_LENGTH,
                DEFAULT_BODY_WRAP_LENGTH, DEFAULT_ISSUE_FOOTER_KEYWORD, false, false, false);
    }
}
