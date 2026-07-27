package com.c301.plugin.domain.commit;

import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.utils.StrUtil;

public final class CommitMessageValidator {
    private CommitMessageValidator() {
    }

    public static ValidationResult validate(CommitTypeDomain commitType, String scope, String shortDescription,
                                            CommitMessageRules rules) {
        if (rules.requireCommitType() && commitType == null) {
            return ValidationResult.MISSING_COMMIT_TYPE;
        }
        if (rules.requireScope() && StrUtil.isBlank(scope)) {
            return ValidationResult.MISSING_SCOPE;
        }
        if (StrUtil.isBlank(shortDescription)) {
            return ValidationResult.MISSING_SHORT_DESCRIPTION;
        }
        if (shortDescription.trim().length() > rules.subjectMaxLength()) {
            return ValidationResult.SUBJECT_TOO_LONG;
        }
        if (rules.forbidSubjectTrailingPeriod() && shortDescription.trim().endsWith(".")) {
            return ValidationResult.SUBJECT_TRAILING_PERIOD;
        }
        return ValidationResult.VALID;
    }

    public static ValidationResult validate(CommitTypeDomain commitType, String shortDescription) {
        return validate(commitType, null, shortDescription, CommitMessageRules.defaults());
    }

    public enum ValidationResult {
        VALID,
        MISSING_COMMIT_TYPE,
        MISSING_SCOPE,
        MISSING_SHORT_DESCRIPTION,
        SUBJECT_TOO_LONG,
        SUBJECT_TRAILING_PERIOD;

        public boolean isValid() {
            return this == VALID;
        }
    }
}
