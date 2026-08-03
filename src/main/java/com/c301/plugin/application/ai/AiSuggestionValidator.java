package com.c301.plugin.application.ai;

import com.c301.plugin.config.EffectiveCommitTemplateSettings;
import com.c301.plugin.domain.ai.AiCommitSuggestion;
import com.c301.plugin.domain.commit.CommitMessageValidator;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitCommitDomain;

import java.util.LinkedList;
import java.util.List;

/**
 * 在任何 UI 回填前，对 AI 候选执行本地白名单与提交规则校验。
 */
public final class AiSuggestionValidator {
    private AiSuggestionValidator() {
    }

    public static GitCommitDomain validateAndConvert(AiCommitSuggestion suggestion,
                                                     EffectiveCommitTemplateSettings settings,
                                                     List<CommitTypeDomain> allowedTypes) {
        CommitTypeDomain type = allowedTypes.stream()
                .filter(item -> item.getType().equalsIgnoreCase(suggestion.type()))
                .findFirst()
                .orElse(null);
        if (type == null) {
            throw new IllegalArgumentException("AI 返回的提交类型不在当前模板允许列表中。");
        }
        CommitMessageValidator.ValidationResult validation = CommitMessageValidator.validate(type, suggestion.scope(),
                suggestion.subject(), settings.commitMessageRules());
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validationMessage(validation));
        }
        GitCommitDomain commit = new GitCommitDomain();
        commit.setCommitType(type);
        commit.setChangeScope(suggestion.scope());
        commit.setShortDescription(suggestion.subject());
        commit.setLongDescription(suggestion.body());
        commit.setBreakingChanges(suggestion.breakingChange());
        commit.setClosedIssues(new LinkedList<>(suggestion.issueNumbers()));
        return commit;
    }

    private static String validationMessage(CommitMessageValidator.ValidationResult result) {
        return switch (result) {
            case MISSING_COMMIT_TYPE -> "AI 返回内容缺少提交类型。";
            case MISSING_SCOPE -> "AI 返回内容缺少必填的 Scope。";
            case MISSING_SHORT_DESCRIPTION -> "AI 返回内容缺少提交标题。";
            case SUBJECT_TOO_LONG -> "AI 返回的提交标题超过当前长度限制。";
            case SUBJECT_TRAILING_PERIOD -> "AI 返回的提交标题末尾不符合当前规则。";
            case VALID -> "AI 返回内容未通过本地提交规则校验。";
        };
    }
}
