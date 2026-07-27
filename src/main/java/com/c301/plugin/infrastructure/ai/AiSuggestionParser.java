package com.c301.plugin.infrastructure.ai;

import com.c301.plugin.domain.ai.AiCommitSuggestion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/** 解析完整流式文本中的 JSON 候选；解析失败时不自动回填任何提交字段。 */
public final class AiSuggestionParser {
    private static final ObjectMapper JSON = new ObjectMapper();

    private AiSuggestionParser() {
    }

    public static AiCommitSuggestion parse(String response) throws Exception {
        String json = response == null ? "" : response.trim()
                .replaceFirst("^```(?:json)?\\s*", "")
                .replaceFirst("\\s*```$", "");
        JsonNode root = JSON.readTree(json);
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

    private static String text(JsonNode root, String field) {
        return root.path(field).asText("").trim();
    }

    private static String nullableText(JsonNode root, String field) {
        return root.path(field).isNull() ? null : text(root, field);
    }
}
