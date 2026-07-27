package com.c301.plugin.domain.ai;

/**
 * 仅在内存中使用的认证信息。禁止记录、序列化或拼接到异常日志中。
 */
public record AiCredentials(String apiKey) {
}
