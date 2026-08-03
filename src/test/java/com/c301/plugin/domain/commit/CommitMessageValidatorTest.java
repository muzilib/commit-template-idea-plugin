package com.c301.plugin.domain.commit;

import com.c301.plugin.model.CommitTypeDomain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommitMessageValidatorTest {
    @Test
    void rejectsMissingCommitType() {
        assertEquals(CommitMessageValidator.ValidationResult.MISSING_COMMIT_TYPE,
                CommitMessageValidator.validate(null, "add project configuration"));
    }

    @Test
    void rejectsBlankShortDescription() {
        assertEquals(CommitMessageValidator.ValidationResult.MISSING_SHORT_DESCRIPTION,
                CommitMessageValidator.validate(new CommitTypeDomain(), "  "));
    }

    @Test
    void acceptsCompleteHeader() {
        assertEquals(CommitMessageValidator.ValidationResult.VALID,
                CommitMessageValidator.validate(new CommitTypeDomain(), "add project configuration"));
    }

    @Test
    void rejectsSubjectLongerThanConfiguredLimit() {
        var rules = new CommitMessageRules(true, false, 50, 72, "Closes", false, true);
        assertEquals(CommitMessageValidator.ValidationResult.SUBJECT_TOO_LONG,
                CommitMessageValidator.validate(new CommitTypeDomain(), "", "a".repeat(51), rules));
    }

    @Test
    void requiresScopeWhenConfigured() {
        var rules = new CommitMessageRules(true, true, 50, 72, "Closes", false);
        assertEquals(CommitMessageValidator.ValidationResult.MISSING_SCOPE,
                CommitMessageValidator.validate(new CommitTypeDomain(), "", "add preview", rules));
    }
}
