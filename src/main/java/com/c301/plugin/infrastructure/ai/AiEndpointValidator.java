package com.c301.plugin.infrastructure.ai;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * 统一校验 AI endpoint，避免设置页与实际请求使用不同的 URL 安全规则。
 */
public final class AiEndpointValidator {
    private AiEndpointValidator() {
    }

    public static boolean isSupported(String value) {
        try {
            URI uri = parse(value);
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            if ("https".equals(scheme)) {
                return true;
            }
            return "http".equals(scheme) && isLoopbackHost(uri.getHost());
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static String requireSupported(String value) {
        if (!isSupported(value)) {
            throw new IllegalArgumentException("AI endpoint 必须使用 HTTPS，或使用 localhost 回环地址的 HTTP。");
        }
        return value.trim();
    }

    private static URI parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AI endpoint 不能为空。");
        }
        try {
            URI uri = new URI(value.trim());
            if (uri.getScheme() == null || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("AI endpoint 格式无效。");
            }
            return uri;
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("AI endpoint 格式无效。", exception);
        }
    }

    private static boolean isLoopbackHost(String host) {
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "[::1]".equals(host)
                || "::1".equals(host);
    }
}
