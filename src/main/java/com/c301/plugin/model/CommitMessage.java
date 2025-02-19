package com.c301.plugin.model;

import com.c301.plugin.utils.StrUtil;

import static com.c301.plugin.constant.Constant.*;

/**
 * 提交信息
 *
 * @Title CommitMessage
 * @ClassName com.c301.plugin.model.CommitMessage
 * @Author Damien Arrachequesne <damien.arrachequesne@gmail.com> Chenbing
 * @Date 25 /02/19 16:32
 * @Version 1.0
 */
public class CommitMessage {

    /**
     * 提交类型
     */
    private ChangeTypeEnum changeType;
    /**
     * 变更范围或模块
     */
    private String changeScope;
    /**
     * 简短说明
     */
    private String shortDescription;
    /**
     * 详细说明
     */
    private String longDescription;
    private String breakingChanges;
    /**
     * 关闭问题，例如#1234
     */
    private String closedIssues;
    /**
     * 文字过长是否换行
     */
    private boolean wrapText = true;
    /**
     * 跳过CI
     */
    private boolean skipCI = false;


    /**
     * 解析提交信息
     *
     * @param rawMessage 文本信息
     * @return 消息对象
     */
    public static CommitMessage parseRawMessage(String rawMessage) {
        var commitMessage = new CommitMessage();
        try {
            //判断是否符合标准信息格式
            var matcher = COMMIT_FIRST_LINE_FORMAT.matcher(rawMessage);
            if (!matcher.find()) return commitMessage;

            //设置变更类型和短描述
            commitMessage.setChangeType(ChangeTypeEnum.valueOf(matcher.group(1).toUpperCase()));
            commitMessage.setChangeScope(matcher.group(3));
            commitMessage.setShortDescription(matcher.group(4));

            //解析剩余信息
            var strings = rawMessage.split(CHAR_LINE);
            if (strings.length < 2) return commitMessage;

            //设置长描述
            var pos = 1;
            var stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                var lineString = strings[pos];
                if (lineString.startsWith(STR_BREAKING) || lineString.startsWith(STR_CLOSES) || lineString.equalsIgnoreCase(SKIP_CI)) {
                    break;
                }

                stringBuilder.append(lineString).append(CHAR_LINE);
            }
            commitMessage.setLongDescription(stringBuilder.toString().trim());

            //设置重大变化
            stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                var lineString = strings[pos];
                if (lineString.startsWith(STR_CLOSES) || lineString.equalsIgnoreCase(SKIP_CI)) {
                    break;
                }

                stringBuilder.append(lineString).append(CHAR_LINE);
            }
            commitMessage.setBreakingChanges(stringBuilder.toString().trim().replace(STR_BREAKING_CHANGE, ""));

            //设备关闭 issues
            matcher = COMMIT_CLOSES_FORMAT.matcher(rawMessage);
            stringBuilder = new StringBuilder();
            while (matcher.find()) {
                stringBuilder.append(matcher.group(1)).append(',');
            }
            if (stringBuilder.length() != 0) {
                stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            }
            commitMessage.setClosedIssues(stringBuilder.toString());
            commitMessage.setSkipCI(rawMessage.contains(SKIP_CI));
            commitMessage.setWrapText(true);
        } catch (RuntimeException ignore) {
        }
        return commitMessage;
    }

    /**
     * 将提交对象转为标准字符串
     *
     * @return 转换后字符串对象
     */
    public String toRwaString() {
        var builder = new StringBuilder();
        builder.append(changeType.getCode());

        if (StrUtil.isNotBlank(changeScope)) {
            builder.append('(').append(changeScope).append(')');
        }

        builder.append(": ").append(shortDescription);

        if (StrUtil.isNotBlank(longDescription)) {
            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(wrapText ? StrUtil.wrap(longDescription, MAX_LINE_LENGTH) : longDescription);
        }

        if (StrUtil.isNotBlank(breakingChanges)) {
            var content = STR_BREAKING_CHANGE + breakingChanges;

            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(wrapText ? StrUtil.wrap(content, MAX_LINE_LENGTH) : content);
        }

        if (StrUtil.isNotBlank(closedIssues)) {
            builder.append(System.lineSeparator());

            for (String closedIssue : closedIssues.split(",")) {
                var formatClosedIssue = StrUtil.isNumeric(closedIssue) ? "#" + closedIssue : closedIssue;

                builder.append(System.lineSeparator())
                        .append(STR_CLOSES)
                        .append(" ")
                        .append(formatClosedIssue);
            }
        }

        if (skipCI) {
            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(SKIP_CI);
        }

        return builder.toString();
    }


    /**
     * Gets change type.
     *
     * @return the change type
     */
    public ChangeTypeEnum getChangeType() {
        return changeType;
    }

    /**
     * Sets change type.
     *
     * @param changeType the change type
     */
    public void setChangeType(ChangeTypeEnum changeType) {
        this.changeType = changeType;
    }

    /**
     * Gets change scope.
     *
     * @return the change scope
     */
    public String getChangeScope() {
        return changeScope;
    }

    /**
     * Sets change scope.
     *
     * @param changeScope the change scope
     */
    public void setChangeScope(String changeScope) {
        this.changeScope = changeScope;
    }

    /**
     * Gets short description.
     *
     * @return the short description
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Sets short description.
     *
     * @param shortDescription the short description
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Gets long description.
     *
     * @return the long description
     */
    public String getLongDescription() {
        return longDescription;
    }

    /**
     * Sets long description.
     *
     * @param longDescription the long description
     */
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    /**
     * Gets breaking changes.
     *
     * @return the breaking changes
     */
    public String getBreakingChanges() {
        return breakingChanges;
    }

    /**
     * Sets breaking changes.
     *
     * @param breakingChanges the breaking changes
     */
    public void setBreakingChanges(String breakingChanges) {
        this.breakingChanges = breakingChanges;
    }

    /**
     * Gets closed issues.
     *
     * @return the closed issues
     */
    public String getClosedIssues() {
        return closedIssues;
    }

    /**
     * Sets closed issues.
     *
     * @param closedIssues the closed issues
     */
    public void setClosedIssues(String closedIssues) {
        this.closedIssues = closedIssues;
    }

    /**
     * Is wrap text boolean.
     *
     * @return the boolean
     */
    public boolean isWrapText() {
        return wrapText;
    }

    /**
     * Sets wrap text.
     *
     * @param wrapText the wrap text
     */
    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
    }

    /**
     * Is skip ci boolean.
     *
     * @return the boolean
     */
    public boolean isSkipCI() {
        return skipCI;
    }

    /**
     * Sets skip ci.
     *
     * @param skipCI the skip ci
     */
    public void setSkipCI(boolean skipCI) {
        this.skipCI = skipCI;
    }

}
