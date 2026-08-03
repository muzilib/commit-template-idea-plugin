package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.domain.ai.OpenAiGenerationOptions;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 构造 OpenAI Responses API 请求，确保请求预览与实际网络传输一致。
 */
public final class OpenAiResponsesRequestRenderer {
    private static final ObjectMapper JSON = new ObjectMapper();

    private OpenAiResponsesRequestRenderer() {
    }

    public static String resolveUrl(AiGenerationRequest request) {
        return AiEndpointValidator.requireSupported(request.apiUrl());
    }

    public static String requestBody(AiGenerationRequest request) throws Exception {
        return JSON.writeValueAsString(requestPayload(request));
    }

    public static String formattedRequestBody(AiGenerationRequest request) throws Exception {
        return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(requestPayload(request));
    }

    private static Map<String, Object> requestPayload(AiGenerationRequest request) {
        OpenAiGenerationOptions options = request.openAiGenerationOptions();
        if (options == null) {
            options = new OpenAiGenerationOptions();
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", request.model());
        payload.put("stream", true);
        payload.put("store", options.isStoreResponse());
        payload.put("instructions", AiPromptRenderer.systemPrompt(request));
        payload.put("input", List.of(Map.of("role", "user", "content", AiPromptRenderer.userPrompt(request))));
        payload.put("max_output_tokens", request.maxTokens());
        putIfPresent(payload, "top_p", options.getTopP());
        payload.put("reasoning", Map.of("effort", defaultReasoningEffort(options.getReasoningEffort())));
        payload.put("text", Map.of("verbosity", defaultVerbosity(options.getVerbosity())));
        return payload;
    }

    private static void putIfPresent(Map<String, Object> payload, String key, Object value) {
        if (value != null) {
            payload.put(key, value);
        }
    }

    private static String defaultReasoningEffort(String value) {
        return value == null || value.isBlank() ? "low" : value;
    }

    private static String defaultVerbosity(String value) {
        return value == null || value.isBlank() ? "low" : value;
    }
}
