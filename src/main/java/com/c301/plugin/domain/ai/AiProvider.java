package com.c301.plugin.domain.ai;

import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.http.client.methods.HttpPost;

import java.util.Map;

/**
 * 标准 AI Provider 抽象。Provider 仅生成文本，不能访问 Git 提交或 Swing UI。
 */
public interface AiProvider {
    void generate(AiGenerationRequest request, AiCredentials credentials,
                  ProgressIndicator indicator, AiStreamingListener listener);

    /**
     * 在通用 OpenAI Compatible 请求体基础上追加服务商专属参数。
     */
    default void customizeRequestPayload(Map<String, Object> payload, AiGenerationRequest request) {
    }

    /**
     * 追加服务商专属请求头，不能覆盖通用鉴权头。
     */
    default void customizeRequestHeaders(HttpPost post, AiGenerationRequest request) {
    }
}
