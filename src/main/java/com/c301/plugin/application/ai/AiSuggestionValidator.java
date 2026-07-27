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
            throw new IllegalArgumentException("AI 返回了当前提交模板中不存在的提交类型。");
        }
        if (!CommitMessageValidator.validate(type, suggestion.scope(), suggestion.subject(),
                settings.commitMessageRules()).isValid()) {
            throw new IllegalArgumentException("AI 返回的提交建议不符合当前提交规则。");
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
}
