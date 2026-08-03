package com.c301.plugin.domain.ai;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeepSeek Chat Completions 的专属可选参数。
 * null 表示不发送该字段，由 DeepSeek 服务使用自身默认值。
 */
@Data
@NoArgsConstructor
public class DeepSeekGenerationOptions {
    private boolean includeUsage = true;
    private Double topP;
    /**
     * 默认关闭思考模式，避免提交信息生成的可见输出预算被 reasoning_content 占用。
     */
    private boolean enableThinking;
    private String reasoningEffort = "high";
}
