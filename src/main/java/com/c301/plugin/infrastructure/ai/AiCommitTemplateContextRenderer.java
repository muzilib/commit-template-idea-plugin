package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.config.EffectiveCommitTemplateSettings;
import com.c301.plugin.model.CommitTypeDomain;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 将当前项目最终生效的提交模板规则转换为稳定的模型上下文，避免模型依赖 UI 状态。
 */
public final class AiCommitTemplateContextRenderer {
    private AiCommitTemplateContextRenderer() {
    }

    public static String render(EffectiveCommitTemplateSettings settings, List<CommitTypeDomain> allowedTypes) {
        String types = allowedTypes.stream().map(CommitTypeDomain::getType).collect(Collectors.joining(", "));
        return "提交内容语言：" + settings.language().getLabel() + "（" + settings.language().getKey() + "）"
                + "\n允许的提交类型：" + types
                + "\n变更范围 Scope：" + (settings.commitMessageRules().requireScope() ? "必填" : "可选")
                + "\n标题最大长度：" + settings.commitMessageRules().subjectMaxLength()
                + "\n标题末尾句号：" + (settings.commitMessageRules().forbidSubjectTrailingPeriod() ? "不允许" : "允许")
                + "\nGitmoji：由本地格式化器决定，模型不得生成"
                + "\n最终格式、footer 和换行：由本地提交规则校验并格式化";
    }
}
