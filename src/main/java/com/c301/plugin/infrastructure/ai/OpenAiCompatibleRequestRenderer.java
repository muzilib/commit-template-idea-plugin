package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.domain.ai.AiProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一构造 OpenAI 兼容请求，确保用户审阅内容与实际网络请求保持一致。
 */
public final class OpenAiCompatibleRequestRenderer {
    private static final ObjectMapper JSON = new ObjectMapper();

    private OpenAiCompatibleRequestRenderer() {
    }

    public static String resolveUrl(AiGenerationRequest request) {
        return AiEndpointValidator.requireSupported(request.apiUrl());
    }

    public static String requestBody(AiGenerationRequest request, AiProvider provider) throws Exception {
        return JSON.writeValueAsString(requestPayload(request, provider));
    }

    public static String formattedRequestBody(AiGenerationRequest request, AiProvider provider) throws Exception {
        return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(requestPayload(request, provider));
    }

    private static Map<String, Object> requestPayload(AiGenerationRequest request, AiProvider provider) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", request.model());
        payload.put("stream", true);
        payload.put("temperature", request.temperature());
        payload.put("max_tokens", request.maxTokens());
        payload.put("messages", List.of(
                Map.of("role", "system", "content", AiPromptRenderer.systemPrompt(request)),
                Map.of("role", "user", "content", AiPromptRenderer.userPrompt(request))
        ));
        provider.customizeRequestPayload(payload, request);
        return payload;
    }
}
