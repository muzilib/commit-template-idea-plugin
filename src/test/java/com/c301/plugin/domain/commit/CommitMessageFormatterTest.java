package com.c301.plugin.domain.commit;

import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitCommitDomain;
import com.c301.plugin.model.GitmojiDomain;
import com.c301.plugin.model.GitmojiLocationDomain;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommitMessageFormatterTest {
    @Test
    void formatsCompleteCommitWithGitmojiAndFooters() {
        var commit = new GitCommitDomain();
        commit.setCommitType(new CommitTypeDomain("feat", new GitmojiDomain(":sparkles:", "sparkles", "✨", "New feature")));
        commit.setChangeScope("api");
        commit.setShortDescription("add commit preview");
        commit.setLongDescription("Show the generated message before applying it.");
        commit.setBreakingChanges("The old dialog contract is removed.");
        commit.setClosedIssues(List.of(12, 34));
        commit.setSkipCI(true);

        var expected = "✨feat(api): add commit preview\n\n"
                + "Show the generated message before applying it.\n\n"
                + "BREAKING CHANGE: The old dialog contract is removed.\n\n"
                + "Closes #12\nCloses #34\n\n[skip ci]";

        assertEquals(expected, CommitMessageFormatter.format(commit, GitmojiLocationDomain.LOCATION1));
    }

    @Test
    void usesConfiguredIssueFooterKeyword() {
        var commit = new GitCommitDomain();
        commit.setCommitType(new CommitTypeDomain("fix", null));
        commit.setShortDescription("handle validation");
        commit.setClosedIssues(List.of(8));
        var rules = new CommitMessageRules(true, false, 50, 72, "Fixes", false);

        assertEquals("fix: handle validation\n\nFixes #8",
                CommitMessageFormatter.format(commit, null, rules));
    }

    @Test
    void omitsGitmojiWhenCommitTypeHasNoEmoji() {
        var commit = new GitCommitDomain();
        commit.setCommitType(new CommitTypeDomain("fix", null));
        commit.setShortDescription("handle missing emoji");

        assertEquals("fix: handle missing emoji", CommitMessageFormatter.format(commit, GitmojiLocationDomain.LOCATION3));
    }
}
