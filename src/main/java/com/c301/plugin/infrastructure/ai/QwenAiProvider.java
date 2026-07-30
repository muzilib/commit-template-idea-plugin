package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.domain.ai.QwenGenerationOptions;
import org.apache.http.client.methods.HttpPost;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通义千问服务商实现，负责追加千问专属文本生成参数与安全请求头。
 */
public final class QwenAiProvider extends OpenAiCompatibleProvider {
    private static void putIfPresent(Map<String, Object> payload, String key, Object value) {
        if (value != null) {
            payload.put(key, value);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public void customizeRequestPayload(Map<String, Object> payload, AiGenerationRequest request) {
        QwenGenerationOptions options = request.qwenGenerationOptions();
        if (options == null) {
            return;
        }
        payload.remove("max_tokens");
        payload.put("max_completion_tokens", request.maxTokens());
        payload.put("stream_options", Map.of("include_usage", options.isIncludeUsage()));
        putIfPresent(payload, "top_p", options.getTopP());
        putIfPresent(payload, "top_k", options.getTopK());
        putIfPresent(payload, "repetition_penalty", options.getRepetitionPenalty());
        putIfPresent(payload, "presence_penalty", options.getPresencePenalty());
        putIfPresent(payload, "seed", options.getSeed());
        // 必须显式关闭思考模式，避免支持混合思考的模型将全部输出预算用于 reasoning_content。
        payload.put("enable_thinking", options.isEnableThinking());
        if (options.isEnableThinking()) {
            putIfPresent(payload, "thinking_budget", options.getThinkingBudget());
            putIfPresent(payload, "reasoning_effort", blankToNull(options.getReasoningEffort()));
        }
        if (options.isEnableSearch()) {
            payload.put("enable_search", true);
            Map<String, Object> searchOptions = new LinkedHashMap<>();
            searchOptions.put("forced_search", options.isForceSearch());
            String strategy = blankToNull(options.getSearchStrategy());
            if (strategy != null) {
                searchOptions.put("search_strategy", strategy);
            }
            payload.put("search_options", searchOptions);
        }
    }

    @Override
    public void customizeRequestHeaders(HttpPost post, AiGenerationRequest request) {
        QwenGenerationOptions options = request.qwenGenerationOptions();
        if (options != null && options.isDataInspectionEnabled()) {
            post.setHeader("X-DashScope-DataInspection", "{\"input\":\"cip\",\"output\":\"cip\"}");
        }
    }
}
