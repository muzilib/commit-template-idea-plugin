package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiProviderType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import java.util.Map;

/**
 * 从内置 TOML 读取不同供应商的默认系统提示词，用户配置可按供应商覆盖默认模板。
 */
public final class AiSystemPromptTemplates {
    private static final String RESOURCE_PATH = "/ai-system-prompt.toml";
    private static final Map<String, String> TEMPLATES = loadTemplates();

    private AiSystemPromptTemplates() {
    }

    public static String forProvider(AiProviderType provider) {
        return template((provider == null ? AiProviderType.CUSTOM : provider).promptTemplate());
    }

    private static String template(String provider) {
        String template = TEMPLATES.get(provider);
        if (template == null || template.isBlank()) {
            throw new IllegalStateException("AI 系统提示词资源缺少 " + provider + " 模板。");
        }
        return template;
    }

    private static Map<String, String> loadTemplates() {
        InputStream stream = AiSystemPromptTemplates.class.getResourceAsStream(RESOURCE_PATH);
        if (stream == null) {
            throw new IllegalStateException("找不到 AI 系统提示词资源：" + RESOURCE_PATH);
        }
        Map<String, String> templates = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String section = null;
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    section = trimmed.substring(1, trimmed.length() - 1).trim();
                    continue;
                }
                if (section == null || !trimmed.equals("template = \"\"\"")) {
                    continue;
                }
                StringBuilder value = new StringBuilder();
                while ((line = reader.readLine()) != null && !line.trim().equals("\"\"\"")) {
                    if (!value.isEmpty()) {
                        value.append('\n');
                    }
                    value.append(line);
                }
                templates.put(section, value.toString().trim());
            }
        } catch (Exception exception) {
            throw new IllegalStateException("无法读取 AI 系统提示词资源。", exception);
        }
        return Map.copyOf(templates);
    }
}
