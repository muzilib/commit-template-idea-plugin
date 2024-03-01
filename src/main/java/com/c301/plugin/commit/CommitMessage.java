package com.c301.plugin.commit;

import com.c301.plugin.localization.PluginBundle;
import com.c301.plugin.utils.StrUtil;

import static com.c301.plugin.constant.Constant.*;

/**
 * @author Damien Arrachequesne <damien.arrachequesne@gmail.com> Chenbing
 */
class CommitMessage {
    private static final int MAX_LINE_LENGTH = 72; // https://stackoverflow.com/a/2120040/5138796

    private ChangeType changeType;
    private String changeScope, shortDescription, longDescription, breakingChanges, closedIssues;
    private boolean wrapText = true;
    private boolean skipCI = false;

    private CommitMessage() {
        this.closedIssues = "";
        this.longDescription = "";
        this.breakingChanges = "";
        this.shortDescription = "";
        this.changeType = ChangeType.FEAT;
    }

    public CommitMessage(ChangeType changeType, String changeScope, String shortDescription, String longDescription, String breakingChanges, String closedIssues, boolean wrapText, boolean skipCI) {
        this.skipCI = skipCI;
        this.wrapText = wrapText;
        this.changeType = changeType;
        this.changeScope = changeScope;
        this.closedIssues = closedIssues;
        this.longDescription = longDescription;
        this.breakingChanges = breakingChanges;
        this.shortDescription = shortDescription;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(changeType.label());
        if (StrUtil.isNotBlank(changeScope)) {
            builder.append('(')
                    .append(changeScope)
                    .append(')');
        }
        builder.append(": ")
                .append(shortDescription);

        if (StrUtil.isNotBlank(longDescription)) {
            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(wrapText ? StrUtil.wrap(longDescription, MAX_LINE_LENGTH) : longDescription);
        }

        if (StrUtil.isNotBlank(breakingChanges)) {
            var content = PluginBundle.get("plugin.commit.breaking_change") + ": " + breakingChanges;
            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(wrapText ? StrUtil.wrap(content, MAX_LINE_LENGTH) : content);
        }

        if (StrUtil.isNotBlank(closedIssues)) {
            builder.append(System.lineSeparator());
            for (String closedIssue : closedIssues.split(",")) {
                builder.append(System.lineSeparator())
                        .append(PluginBundle.get("plugin.commit.closes"))
                        .append(" ")
                        .append(formatClosedIssue(closedIssue));
            }
        }

        if (skipCI) {
            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(SKIP_CI);
        }

        return builder.toString();
    }

    private String formatClosedIssue(String closedIssue) {
        var trimmed = closedIssue.trim();
        return (StrUtil.isNumeric(trimmed) ? "#" : "") + trimmed;
    }

    public static CommitMessage parse(String message) {
        var commitMessage = new CommitMessage();
        var STR_CLOSES = PluginBundle.get("plugin.commit.closes");
        var STR_BREAKING = PluginBundle.get("plugin.commit.breaking");
        var STR_BREAKING_CHANGE = PluginBundle.get("plugin.commit.breaking_change");

        try {
            var matcher = COMMIT_FIRST_LINE_FORMAT.matcher(message);
            if (!matcher.find()) return commitMessage;

            commitMessage.changeType = ChangeType.valueOf(matcher.group(1).toUpperCase());
            commitMessage.changeScope = matcher.group(3);
            commitMessage.shortDescription = matcher.group(4);

            String[] strings = message.split("\n");
            if (strings.length < 2) return commitMessage;

            var pos = 1;
            var stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                String lineString = strings[pos];
                if (lineString.startsWith(STR_BREAKING) || lineString.startsWith(STR_CLOSES) || lineString.equalsIgnoreCase(SKIP_CI))
                    break;
                stringBuilder.append(lineString).append('\n');
            }
            commitMessage.longDescription = stringBuilder.toString().trim();

            stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                String lineString = strings[pos];
                if (lineString.startsWith(STR_CLOSES) || lineString.equalsIgnoreCase(SKIP_CI)) break;
                stringBuilder.append(lineString).append('\n');
            }
            commitMessage.breakingChanges = stringBuilder.toString().trim().replace(STR_BREAKING_CHANGE + ": ", "");

            matcher = COMMIT_CLOSES_FORMAT.matcher(message);
            stringBuilder = new StringBuilder();
            while (matcher.find()) {
                stringBuilder.append(matcher.group(1)).append(',');
            }
            if (stringBuilder.length() != 0) stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            commitMessage.closedIssues = stringBuilder.toString();
            commitMessage.skipCI = message.contains(SKIP_CI);
        } catch (RuntimeException ignore) {
        }
        return commitMessage;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public String getChangeScope() {
        return changeScope;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getBreakingChanges() {
        return breakingChanges;
    }

    public String getClosedIssues() {
        return closedIssues;
    }

    public boolean isSkipCI() {
        return skipCI;
    }

}
