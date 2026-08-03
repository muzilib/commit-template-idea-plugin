package com.c301.plugin.domain.ai;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenAI Responses API 的专属可选参数。
 */
@Data
@NoArgsConstructor
public class OpenAiGenerationOptions {
    /** GPT-5 系列生成提交信息时的推荐默认推理强度。 */
    private String reasoningEffort = "low";
    /** 提交信息应保持简洁。 */
    private String verbosity = "low";
    /** null 表示不发送，使用模型默认采样行为。 */
    private Double topP;
    /** 默认不存储请求响应。 */
    private boolean storeResponse;
}
