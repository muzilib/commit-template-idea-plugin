package com.c301.plugin.domain.commit;

import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitCommitDomain;
import com.c301.plugin.model.GitmojiLocationDomain;
import com.c301.plugin.utils.StrUtil;

/**
 * 根据结构化的提交表单数据生成提交信息文本。
 */
public final class CommitMessageFormatter {
    private CommitMessageFormatter() {
    }

    public static String format(GitCommitDomain commit, GitmojiLocationDomain emojiLocation) {
        return format(commit, emojiLocation, CommitMessageRules.defaults());
    }

    public static String format(GitCommitDomain commit, GitmojiLocationDomain emojiLocation,
                                CommitMessageRules rules) {
        var builder = new StringBuilder();
        var commitType = commit.getCommitType();

        if (commitType != null) {
            if (isEmojiAt(emojiLocation, GitmojiLocationDomain.LOCATION1, commitType)) {
                builder.append(commitType.getEmoji().getEmoji());
            }
            builder.append(commitType.getType());
        }

        if (StrUtil.isNotBlank(commit.getChangeScope())) {
            builder.append("(");
            if (isEmojiAt(emojiLocation, GitmojiLocationDomain.LOCATION2, commitType)) {
                builder.append(commitType.getEmoji().getEmoji()).append(" ");
            }
            builder.append(commit.getChangeScope().trim()).append("): ");
        } else {
            builder.append(": ");
        }

        if (StrUtil.isNotBlank(commit.getShortDescription())) {
            if (isEmojiAt(emojiLocation, GitmojiLocationDomain.LOCATION3, commitType)) {
                builder.append(commitType.getEmoji().getEmoji());
            }
            builder.append(commit.getShortDescription().trim());
        }

        appendSection(builder, commit.getLongDescription(), commit.isWrapText(), rules.bodyWrapLength(), false);
        appendSection(builder, commit.getBreakingChanges(), commit.isWrapText(), rules.bodyWrapLength(), true);

        if (commit.getClosedIssues() != null && !commit.getClosedIssues().isEmpty()) {
            builder.append(System.lineSeparator());
            for (Integer closedIssue : commit.getClosedIssues()) {
                builder.append(System.lineSeparator())
                        .append(rules.issueFooterKeyword())
                        .append(" #")
                        .append(closedIssue);
            }
        }

        if (commit.isSkipCI()) {
            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("[skip ci]");
        }
        return builder.toString();
    }

    private static boolean isEmojiAt(GitmojiLocationDomain actualLocation,
                                     GitmojiLocationDomain expectedLocation,
                                     CommitTypeDomain commitType) {
        return actualLocation != null
                && actualLocation.equals(expectedLocation)
                && commitType != null
                && commitType.getEmoji() != null
                && StrUtil.isNotBlank(commitType.getEmoji().getEmoji());
    }

    private static void appendSection(StringBuilder builder, String content, boolean wrapText,
                                      int wrapLength, boolean breakingChange) {
        if (StrUtil.isBlank(content)) {
            return;
        }

        var value = breakingChange ? "BREAKING CHANGE: " + content.trim() : content.trim();
        if (wrapText) {
            value = StrUtil.wrap(value, wrapLength);
        }

        builder.append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(value);
    }
}
