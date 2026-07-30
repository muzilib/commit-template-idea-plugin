package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.*;
import com.c301.plugin.utils.CommUtil;
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
import java.nio.charset.StandardCharsets;

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
            listener.onError(new AiGenerationError(AiGenerationError.Kind.CONFIGURATION, text("plugin.ai.error.apiKeyMissing")));
            return;
        }
        try (CloseableHttpClient client = HttpClients.custom()
                .disableAutomaticRetries()
                .build()) {
            HttpPost post = new HttpPost(OpenAiCompatibleRequestRenderer.resolveUrl(request));
            post.setHeader("Authorization", "Bearer " + credentials.apiKey());
            post.setHeader("Accept", "text/event-stream");
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(OpenAiCompatibleRequestRenderer.requestBody(request),
                    ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(post)) {
                int status = response.getStatusLine().getStatusCode();
                if (status < 200 || status >= 300) {
                    listener.onError(new AiGenerationError(AiGenerationError.Kind.PROVIDER,
                            text("plugin.ai.error.provider").replace("{status}", String.valueOf(status))));
                    return;
                }
                if (response.getEntity() == null) {
                    listener.onError(new AiGenerationError(AiGenerationError.Kind.RESPONSE,
                            text("plugin.ai.error.emptyResponse")));
                    return;
                }
                stream(response, indicator, listener);
            }
        } catch (Exception exception) {
            listener.onError(new AiGenerationError(AiGenerationError.Kind.NETWORK,
                    text("plugin.ai.error.network")));
        }
    }

    private static String text(String key) {
        return CommUtil.i18nResourceBundle(null).getString(key);
    }

    private void stream(CloseableHttpResponse response, ProgressIndicator indicator,
                        AiStreamingListener listener) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (indicator.isCanceled()) {
                    listener.onError(new AiGenerationError(AiGenerationError.Kind.CANCELED,
                            text("plugin.ai.error.canceled")));
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
}
