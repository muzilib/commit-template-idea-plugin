package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.domain.ai.DeepSeekGenerationOptions;

import java.util.Map;

/**
 * DeepSeek Chat Completions 服务商实现，负责追加 DeepSeek 官方定义的专属参数。
 */
public final class DeepSeekAiProvider extends OpenAiCompatibleProvider {
    @Override
    public void customizeRequestPayload(Map<String, Object> payload, AiGenerationRequest request) {
        DeepSeekGenerationOptions options = request.deepSeekGenerationOptions();
        if (options == null) {
            options = new DeepSeekGenerationOptions();
        }
        payload.put("stream_options", Map.of("include_usage", options.isIncludeUsage()));
        if (options.getTopP() != null) {
            payload.put("top_p", options.getTopP());
        }
        if (options.isEnableThinking()) {
            String reasoningEffort = options.getReasoningEffort();
            if (reasoningEffort == null || reasoningEffort.isBlank()) {
                reasoningEffort = "high";
            }
            payload.put("thinking", Map.of("type", "enabled", "reasoning_effort", reasoningEffort));
        } else {
            payload.put("thinking", Map.of("type", "disabled"));
        }
    }
}
