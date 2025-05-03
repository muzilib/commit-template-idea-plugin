package com.c301.plugin.model;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.utils.CommUtil;
import com.c301.plugin.utils.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static com.c301.plugin.constant.Constant.*;

/**
 * Git提交日志对象
 *
 * @Title GitCommitDomain
 * @ClassName com.c301.plugin.model.GitCommitDomain
 * @Author Chenbing
 * @Date 25 /03/11 17:36
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitCommitDomain {

    /**
     * 提交类型
     */
    private CommitTypeDomain commitType = null;
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
    /**
     * 重大变化
     */
    private String breakingChanges;
    /**
     * 关闭问题，例如#1234
     */
    private List<Integer> closedIssues = new LinkedList<>();
    /**
     * 文字过长是否换行
     */
    private boolean wrapText = false;
    /**
     * 跳过CI
     */
    private boolean skipCI = false;

    /**
     * 获取关闭问题Git提交字符串格式
     *
     * @return 返回 Closes #1234
     */
    public String getClosedIssuesText() {
        var builder = new StringBuilder();
        for (Integer issue : closedIssues) {
            builder.append(Constant.STR_CLOSES).append(" #").append(issue).append(System.lineSeparator());
        }

        if (!builder.isEmpty()) builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * 获取关闭问题数值列表字符串
     *
     * @return 返回14, 134, 34
     */
    public String getClosedIssuesNumbers() {
        var builder = new StringBuilder();
        for (Integer issue : closedIssues) {
            builder.append(issue).append(", ");
        }

        if (!builder.isEmpty()) builder.deleteCharAt(builder.length() - 2);
        return builder.toString().trim();
    }

    /**
     * 解析原始提交信息
     *
     * @param rawMessage 原始提交信息
     * @return GitCommitDomain对象
     */
    public static GitCommitDomain parseRawMessage(String rawMessage) {
        var gitCommit = new GitCommitDomain();
        if (StrUtil.isBlank(rawMessage)) return gitCommit;

        try {
            // 去除第一个emoji
            var pattern = Pattern.compile("[\\x{1F300}-\\x{1F5FF}\\x{1F600}-\\x{1F64F}\\x{1F680}-\\x{1F6FF}\\x{2600}-\\x{26FF}\\x{2700}-\\x{27BF}\\x{FE00}-\\x{FE0F}]", Pattern.UNICODE_CHARACTER_CLASS);
            var matcher = pattern.matcher(rawMessage);
            if (matcher.find()) rawMessage = matcher.replaceAll("");

            // 在正则前统一处理换行符
            rawMessage = rawMessage.replaceAll("\\r\\n?", "\n");
            // 修改正则表达式以支持两种格式：
            // 1. style(搭建项目): 处理服务注册失败的问题
            // 2. style: 处理服务注册失败的问题
            pattern = Pattern.compile("^([a-zA-Z0-9\\u4e00-\\u9fa5-]+)(?:\\(([^()]+)\\))?:\\s+([^\\n]+)", Pattern.UNICODE_CHARACTER_CLASS);
            matcher = pattern.matcher(rawMessage);
            if (!matcher.find()) return gitCommit;

            //解析第一行内容
            gitCommit.setCommitType(CommUtil.parseCommitType(matcher.group(1)));
            // 如果group(2)为null，说明没有括号部分
            gitCommit.setChangeScope(matcher.group(2) != null ? matcher.group(2) : "");
            gitCommit.setShortDescription(matcher.group(3));

            //解析剩余信息
            var strings = rawMessage.split(CHAR_LINE);
            if (strings.length < 2) return gitCommit;

            //设置长描述
            var index = 2;
            var builder = new StringBuilder();
            for (; index < strings.length; index++) {
                var line = strings[index];
                if (line.startsWith("BREAKING") || line.startsWith("Closes") || line.equalsIgnoreCase("[skip ci]")) {
                    break;
                }
                builder.append(line);
                if (StrUtil.isNotBlank(line)) builder.append('\n');
            }
            if (!builder.isEmpty()) builder.deleteCharAt(builder.length() - 1);
            gitCommit.setLongDescription(builder.toString());

            //设置重大变化
            builder = new StringBuilder();
            for (; index < strings.length; index++) {
                var line = strings[index];
                if (line.startsWith("Closes") || line.equalsIgnoreCase("[skip ci]")) {
                    break;
                }
                if (line.startsWith("BREAKING CHANGE: ")) line = line.replace("BREAKING CHANGE: ", "");
                builder.append(line);
                if (StrUtil.isNotBlank(line)) builder.append('\n');
            }
            if (!builder.isEmpty()) builder.deleteCharAt(builder.length() - 1);
            gitCommit.setBreakingChanges(builder.toString());

            //获取关闭问题列表
            var closeIssuesList = new LinkedList<Integer>();
            matcher = COMMIT_CLOSES_FORMAT.matcher(rawMessage);
            while (matcher.find()) {
                var issue = matcher.group(1);
                issue = issue.trim().replaceAll("#", "");

                if (!StrUtil.isNumeric(issue)) continue;
                closeIssuesList.add(Integer.parseInt(issue));
            }
            gitCommit.setClosedIssues(closeIssuesList);

            gitCommit.setSkipCI(rawMessage.contains(SKIP_CI));
            gitCommit.setWrapText(false);
        } catch (Exception ignored) {
        }
        return gitCommit;
    }

    /**
     * 生成提交信息
     *
     * @return 提交信息
     */
    public String toStringMessage(GitmojiLocationDomain location) {
        var builder = new StringBuilder();

        //提交类型
        if (commitType != null) {
            if (location != null && location.equals(GitmojiLocationDomain.LOCATION1)) {
                builder.append(commitType.getEmoji().getEmoji());
            }
            builder.append(commitType.getType());
        }

        //变更范围
        if (StrUtil.isNotBlank(changeScope)) {
            var value = changeScope.trim();
            builder.append("(");
            if (location != null && location.equals(GitmojiLocationDomain.LOCATION2)) {
                builder.append(commitType.getEmoji().getEmoji()).append(" ");
            }
            builder.append(value).append("): ");
        } else {
            builder.append(": ");
        }

        //短说明
        if (StrUtil.isNotBlank(shortDescription)) {
            if (location != null && location.equals(GitmojiLocationDomain.LOCATION3)) {
                builder.append(commitType.getEmoji().getEmoji());
            }
            builder.append(shortDescription.trim());
        }

        //长说明
        if (StrUtil.isNotBlank(longDescription)) {
            var value = longDescription.trim();
            if (wrapText) value = StrUtil.wrap(value, MAX_LINE_LENGTH);

            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(value);
        }

        //重大变化
        if (StrUtil.isNotBlank(breakingChanges)) {
            var value = "BREAKING CHANGE: " + breakingChanges.trim();
            if (wrapText) value = StrUtil.wrap(value, MAX_LINE_LENGTH);

            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(value);
        }

        //关闭问题
        if (!closedIssues.isEmpty()) {
            builder.append(System.lineSeparator());

            for (Integer closedIssue : closedIssues) {
                var value = "#" + closedIssue.toString();

                builder.append(System.lineSeparator())
                        .append(STR_CLOSES)
                        .append(" ")
                        .append(value);
            }
        }

        //跳过CI
        if (skipCI) {
            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("[skip ci]");
        }
        return builder.toString();
    }

}
