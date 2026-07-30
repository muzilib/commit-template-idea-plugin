package com.c301.plugin.domain.ai;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通义千问文本生成的专属可选参数。
 * null 表示不发送该字段，由模型服务使用自身默认值。
 */
@Data
@NoArgsConstructor
public class QwenGenerationOptions {
    private boolean includeUsage = true;
    private Double topP;
    private Integer topK;
    private Double repetitionPenalty;
    private Double presencePenalty;
    private Long seed;
    private boolean enableThinking;
    private Integer thinkingBudget;
    private String reasoningEffort;
    private boolean enableSearch;
    private boolean forceSearch;
    private String searchStrategy = "turbo";
    private boolean dataInspectionEnabled;
}
