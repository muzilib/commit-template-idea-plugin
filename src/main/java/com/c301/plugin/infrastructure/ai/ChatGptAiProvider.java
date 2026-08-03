package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiCredentials;
import com.c301.plugin.domain.ai.AiGenerationError;
import com.c301.plugin.domain.ai.AiGenerationRequest;
import com.c301.plugin.domain.ai.AiStreamingListener;
import com.c301.plugin.utils.CommUtil;

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
 * OpenAI Responses API 服务商实现。
 * 仅消费 output_text 增量，绝不将 reasoning 或其他响应项显示为提交信息。
 */
public final class ChatGptAiProvider implements com.c301.plugin.domain.ai.AiProvider {
    private static final ObjectMapper JSON = new ObjectMapper();

    private static String text(String key) {
        return CommUtil.i18nResourceBundle(null).getString(key);
    }

    @Override
    public void generate(AiGenerationRequest request, AiCredentials credentials,
                         ProgressIndicator indicator, AiStreamingListener listener) {
        if (credentials == null || credentials.apiKey() == null || credentials.apiKey().isBlank()) {
            listener.onError(new AiGenerationError(AiGenerationError.Kind.CONFIGURATION, text("plugin.ai.error.apiKeyMissing")));
            return;
        }
        try (CloseableHttpClient client = HttpClients.custom()
                .disableAutomaticRetries()
                .setDefaultRequestConfig(AiHttpRequestSupport.requestConfig())
                .build()) {
            HttpPost post = new HttpPost(OpenAiResponsesRequestRenderer.resolveUrl(request));
            try (AiHttpRequestSupport.CancellationMonitor cancellation =
                         AiHttpRequestSupport.monitorCancellation(post, indicator)) {
                post.setHeader("Authorization", "Bearer " + credentials.apiKey());
                post.setHeader("Accept", "text/event-stream");
                post.setHeader("Content-Type", "application/json");
                post.setEntity(new StringEntity(OpenAiResponsesRequestRenderer.requestBody(request), ContentType.APPLICATION_JSON));
                try (CloseableHttpResponse response = client.execute(post)) {
                    int status = response.getStatusLine().getStatusCode();
                    if (status < 200 || status >= 300) {
                        listener.onError(new AiGenerationError(AiGenerationError.Kind.PROVIDER,
                                text("plugin.ai.error.provider").replace("{status}", String.valueOf(status))));
                        return;
                    }
                    if (response.getEntity() == null) {
                        listener.onError(new AiGenerationError(AiGenerationError.Kind.RESPONSE, text("plugin.ai.error.emptyResponse")));
                        return;
                    }
                    stream(response, cancellation, listener);
                }
            }
        } catch (Exception exception) {
            if (indicator.isCanceled()) {
                listener.onError(new AiGenerationError(AiGenerationError.Kind.CANCELED,
                        text("plugin.ai.error.canceled")));
            } else {
                listener.onError(new AiGenerationError(AiGenerationError.Kind.NETWORK,
                        text("plugin.ai.error.network")));
            }
        }
    }

    private void stream(CloseableHttpResponse response, AiHttpRequestSupport.CancellationMonitor cancellation,
                        AiStreamingListener listener) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            String event = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (cancellation.isCanceled()) {
                    listener.onError(new AiGenerationError(AiGenerationError.Kind.CANCELED, text("plugin.ai.error.canceled")));
                    return;
                }
                if (line.startsWith("event:")) {
                    event = line.substring("event:".length()).trim();
                    continue;
                }
                if (!line.startsWith("data:")) {
                    continue;
                }
                String payload = line.substring("data:".length()).trim();
                if ("response.output_text.delta".equals(event)) {
                    appendTextDelta(payload, listener);
                } else if ("response.completed".equals(event)) {
                    listener.onComplete();
                    return;
                } else if ("response.incomplete".equals(event)) {
                    listener.onError(new AiGenerationError(AiGenerationError.Kind.RESPONSE, text("plugin.ai.openai.error.incomplete")));
                    return;
                } else if ("response.failed".equals(event)) {
                    listener.onError(new AiGenerationError(AiGenerationError.Kind.PROVIDER, text("plugin.ai.openai.error.failed")));
                    return;
                }
            }
        }
        listener.onComplete();
    }

    private void appendTextDelta(String payload, AiStreamingListener listener) {
        try {
            String delta = JSON.readTree(payload).path("delta").asText("");
            if (!delta.isEmpty()) {
                listener.onText(delta);
            }
        } catch (Exception ignored) {
            // 单个无效 SSE 片段不能泄露原始响应，只忽略该片段并继续读取。
        }
    }
}
