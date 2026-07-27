package com.c301.plugin.domain.ai;

import com.intellij.openapi.progress.ProgressIndicator;

/**
 * 标准 AI Provider 抽象。Provider 仅生成文本，不能访问 Git 提交或 Swing UI。
 */
public interface AiProvider {
    void generate(AiGenerationRequest request, AiCredentials credentials,
                  ProgressIndicator indicator, AiStreamingListener listener);
}
