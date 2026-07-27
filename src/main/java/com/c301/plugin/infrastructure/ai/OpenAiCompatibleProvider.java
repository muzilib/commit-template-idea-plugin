package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Chat Completions 兼容协议的 SSE 实现，可对接兼容该标准的模型服务。
 * 请求与响应只保存在内存，异常信息经过脱敏后再交给调用方。
 */
public final class OpenAiCompatibleProvider implements AiProvider {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public void generate(AiGenerationRequest request, AiCredentials credentials,
                         ProgressIndicator indicator, AiStreamingListener listener) {
        if (credentials == null || credentials.apiKey() == null || credentials.apiKey().isBlank()) {
            listener.onError(new AiGenerationError(AiGenerationError.Kind.CONFIGURATION, "未配置 API Key。"));
            return;
        }
        try (CloseableHttpClient client = HttpClients.custom()
                .disableAutomaticRetries()
                .build()) {
            HttpPost post = new HttpPost(resolveUrl(request.endpoint(), request.apiPath()));
            post.setHeader("Authorization", "Bearer " + credentials.apiKey());
            post.setHeader("Accept", "text/event-stream");
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(JSON.writeValueAsString(Map.of(
                    "model", request.model(),
                    "stream", true,
                    "temperature", request.temperature(),
                    "max_tokens", request.maxTokens(),
                    "messages", List.of(
                            Map.of("role", "system", "content", AiPromptRenderer.systemPrompt(request)),
                            Map.of("role", "user", "content", AiPromptRenderer.userPrompt(request))
                    )
            )), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(post)) {
                int status = response.getStatusLine().getStatusCode();
                if (status < 200 || status >= 300) {
                    listener.onError(new AiGenerationError(AiGenerationError.Kind.PROVIDER,
                            "AI 服务请求失败（HTTP " + status + "）。请检查服务地址、模型和密钥。"));
                    return;
                }
                if (response.getEntity() == null) {
                    listener.onError(new AiGenerationError(AiGenerationError.Kind.RESPONSE, "AI 服务未返回内容。"));
                    return;
                }
                stream(response, indicator, listener);
            }
        } catch (Exception exception) {
            listener.onError(new AiGenerationError(AiGenerationError.Kind.NETWORK,
                    "无法连接 AI 服务。请检查网络和服务配置。"));
        }
    }

    private void stream(CloseableHttpResponse response, ProgressIndicator indicator,
                        AiStreamingListener listener) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (indicator.isCanceled()) {
                    listener.onError(new AiGenerationError(AiGenerationError.Kind.CANCELED, "已取消生成。"));
                    return;
                }
                if (!line.startsWith("data:")) {
                    continue;
                }
                String payload = line.substring("data:".length()).trim();
                if ("[DONE]".equals(payload)) {
                    listener.onComplete();
                    return;
                }
                appendDelta(payload, listener);
            }
        }
        listener.onComplete();
    }

    private void appendDelta(String payload, AiStreamingListener listener) {
        try {
            JsonNode root = JSON.readTree(payload);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                String content = choices.get(0).path("delta").path("content").asText("");
                if (!content.isEmpty()) {
                    listener.onText(content);
                }
            }
        } catch (Exception ignored) {
            // 个别无效 SSE 片段不能泄露原始响应，只忽略该片段并继续读取。
        }
    }

    private static String resolveUrl(String endpoint, String apiPath) {
        String base = endpoint == null ? "" : endpoint.trim();
        String path = apiPath == null || apiPath.isBlank() ? "/chat/completions" : apiPath.trim();
        if (!base.endsWith("/") && !path.startsWith("/")) {
            return URI.create(base + "/" + path).toString();
        }
        if (base.endsWith("/") && path.startsWith("/")) {
            return URI.create(base + path.substring(1)).toString();
        }
        return URI.create(base + path).toString();
    }
}
