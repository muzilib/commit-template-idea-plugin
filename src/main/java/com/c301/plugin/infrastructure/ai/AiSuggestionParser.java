package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiCommitSuggestion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析完整流式文本中的 JSON 候选；解析失败时不自动回填任何提交字段。
 */
public final class AiSuggestionParser {
    private static final ObjectMapper JSON = new ObjectMapper();

    private AiSuggestionParser() {
    }

    public static AiCommitSuggestion parse(String response) throws Exception {
        String json = extractJson(response);
        JsonNode root = JSON.readTree(json);
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("AI 返回的内容不是有效的 JSON 对象。");
        }
        List<Integer> issues = new ArrayList<>();
        for (JsonNode issue : root.path("issueNumbers")) {
            if (issue.canConvertToInt()) {
                issues.add(issue.asInt());
            }
        }
        return new AiCommitSuggestion(
                text(root, "type"), text(root, "scope"), text(root, "subject"),
                nullableText(root, "body"), nullableText(root, "breakingChange"), issues
        );
    }

    private static String extractJson(String response) {
        String value = response == null ? "" : response.trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "").trim();
        }
        int objectStart = value.indexOf('{');
        int objectEnd = value.lastIndexOf('}');
        if (objectStart < 0 || objectEnd < objectStart) {
            throw new IllegalArgumentException("AI 未返回可解析的 JSON 对象。");
        }
        return value.substring(objectStart, objectEnd + 1);
    }

    private static String text(JsonNode root, String field) {
        return root.path(field).asText("").trim();
    }

    private static String nullableText(JsonNode root, String field) {
        return root.path(field).isNull() ? null : text(root, field);
    }
}
