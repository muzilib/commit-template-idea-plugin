package com.c301.plugin.domain.commit;

/**
 * Non-sensitive rules that control commit-message validation and formatting.
 */
public record CommitMessageRules(
        boolean requireCommitType,
        boolean requireScope,
        int subjectMaxLength,
        int bodyWrapLength,
        String issueFooterKeyword,
        boolean forbidSubjectTrailingPeriod
) {
    public static final int DEFAULT_SUBJECT_MAX_LENGTH = 50;
    public static final int DEFAULT_BODY_WRAP_LENGTH = 72;
    public static final String DEFAULT_ISSUE_FOOTER_KEYWORD = "Closes";

    public CommitMessageRules {
        subjectMaxLength = subjectMaxLength > 0 ? subjectMaxLength : DEFAULT_SUBJECT_MAX_LENGTH;
        bodyWrapLength = bodyWrapLength > 0 ? bodyWrapLength : DEFAULT_BODY_WRAP_LENGTH;
        issueFooterKeyword = issueFooterKeyword == null || issueFooterKeyword.isBlank()
                ? DEFAULT_ISSUE_FOOTER_KEYWORD
                : issueFooterKeyword.trim();
    }

    public static CommitMessageRules defaults() {
        return new CommitMessageRules(true, false, DEFAULT_SUBJECT_MAX_LENGTH,
                DEFAULT_BODY_WRAP_LENGTH, DEFAULT_ISSUE_FOOTER_KEYWORD, false);
    }
}
