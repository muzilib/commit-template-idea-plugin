package com.c301.plugin.model;

import com.c301.plugin.constant.Constant;
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
            builder.append(Constant.STR_CLOSES)
                    .append(" #")
                    .append(issue)
                    .append(System.lineSeparator());
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

        if (!builder.isEmpty()) builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * 解析原始提交信息
     *
     * @param rawMessage 原始提交信息
     * @return GitCommitDomain对象
     */
    public static GitCommitDomain parseRawMessage(String rawMessage) {
        var gitCommit = new GitCommitDomain();
        if (StrUtil.isNotBlank(rawMessage)) {
            try {
                var matcher = Pattern.compile("^(.+)(\\((.+)\\))?: (.+)").matcher(rawMessage);
                if (!matcher.find()) return gitCommit;

                //解析第一行内容
                gitCommit.setCommitType(CommitTypeDomain.parseCommitType(matcher.group(1)));
                gitCommit.setChangeScope(matcher.group(3));
                gitCommit.setShortDescription(matcher.group(4));

                //解析剩余信息
                var strings = rawMessage.split(CHAR_LINE);
                if (strings.length < 2) return gitCommit;

                //设置长描述
                var index = 1;
                var builder = new StringBuilder();
                for (; index < strings.length; index++) {
                    var line = strings[index];
                    if (line.startsWith("BREAKING") || line.startsWith("Closes") || line.equalsIgnoreCase("[skip ci]")) {
                        break;
                    }
                    builder.append(line).append('\n');
                }
                gitCommit.setLongDescription(builder.toString());

                //设置重大变化

                //gitCommit.setBreakingChanges();

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
        }
        return gitCommit;
    }

}
