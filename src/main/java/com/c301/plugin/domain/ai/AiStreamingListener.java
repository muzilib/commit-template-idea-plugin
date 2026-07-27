package com.c301.plugin.domain.ai;

/**
 * 接收 Provider 的增量文本。所有回调都发生在后台线程，调用方负责切换到 UI 线程。
 */
public interface AiStreamingListener {
    void onText(String text);

    void onComplete();

    void onError(AiGenerationError error);
}
