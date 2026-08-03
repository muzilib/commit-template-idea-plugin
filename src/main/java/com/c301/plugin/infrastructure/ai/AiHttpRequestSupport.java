package com.c301.plugin.infrastructure.ai;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI 流式请求的统一超时和取消支持。
 * 读取超时保留较长窗口，避免正常模型生成间隔被误判为断开。
 */
final class AiHttpRequestSupport {
    private static final int CONNECTION_REQUEST_TIMEOUT_MILLIS = 10_000;
    private static final int CONNECT_TIMEOUT_MILLIS = 15_000;
    private static final int SOCKET_TIMEOUT_MILLIS = 60_000;
    private static final long CANCELLATION_POLL_INTERVAL_MILLIS = 100L;

    private AiHttpRequestSupport() {
    }

    static RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLIS)
                .setConnectTimeout(CONNECT_TIMEOUT_MILLIS)
                .setSocketTimeout(SOCKET_TIMEOUT_MILLIS)
                .build();
    }

    static CancellationMonitor monitorCancellation(HttpPost request, ProgressIndicator indicator) {
        return new CancellationMonitor(request, indicator);
    }

    static final class CancellationMonitor implements AutoCloseable {
        private final HttpPost request;
        private final ProgressIndicator indicator;
        private final AtomicBoolean canceled = new AtomicBoolean();
        private final ScheduledFuture<?> task;

        private CancellationMonitor(HttpPost request, ProgressIndicator indicator) {
            this.request = request;
            this.indicator = indicator;
            this.task = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
                    this::abortWhenCanceled, 0L, CANCELLATION_POLL_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
        }

        boolean isCanceled() {
            return canceled.get() || indicator.isCanceled();
        }

        private void abortWhenCanceled() {
            if (indicator.isCanceled() && canceled.compareAndSet(false, true)) {
                request.abort();
            }
        }

        @Override
        public void close() {
            task.cancel(false);
        }
    }
}
