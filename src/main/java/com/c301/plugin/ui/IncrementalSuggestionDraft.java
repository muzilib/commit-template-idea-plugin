package com.c301.plugin.ui;

/**
 * 从未完成的 JSON 流中读取已闭合的字符串字段，只生成可见提交信息草稿。
 * 不接受未闭合字段，避免把原始 JSON 或转义片段写入 Commit Message。
 */
final class IncrementalSuggestionDraft {
    private IncrementalSuggestionDraft() {
    }

    static String render(String response) {
        String type = completedStringValue(response, "type");
        String scope = completedStringValue(response, "scope");
        String subject = partialStringValue(response, "subject");
        if (type == null || subject == null || type.isBlank() || subject.isBlank()) {
            return "";
        }
        StringBuilder message = new StringBuilder(type);
        if (scope != null && !scope.isBlank()) {
            message.append('(').append(scope).append(')');
        }
        message.append(": ").append(subject);
        String body = partialStringValue(response, "body");
        if (body != null && !body.isBlank()) {
            message.append("\n\n").append(body);
        }
        String breakingChange = partialStringValue(response, "breakingChange");
        if (breakingChange != null && !breakingChange.isBlank()) {
            message.append("\n\nBREAKING CHANGE: ").append(breakingChange);
        }
        return message.toString();
    }

    private static String completedStringValue(String json, String key) {
        return stringValue(json, key, false);
    }

    private static String partialStringValue(String json, String key) {
        return stringValue(json, key, true);
    }

    private static String stringValue(String json, String key, boolean allowPartial) {
        if (json == null || json.isBlank()) {
            return null;
        }
        String marker = "\"" + key + "\"";
        int position = json.indexOf(marker);
        if (position < 0) {
            return null;
        }
        int colon = json.indexOf(':', position + marker.length());
        if (colon < 0) {
            return null;
        }
        int start = skipWhitespace(json, colon + 1);
        if (start >= json.length()) {
            return null;
        }
        if (json.startsWith("null", start)) {
            return null;
        }
        if (json.charAt(start) != '"') {
            return null;
        }
        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int index = start + 1; index < json.length(); index++) {
            char current = json.charAt(index);
            if (escaped) {
                appendEscaped(value, current, json, index);
                if (current == 'u' && index + 4 < json.length()) {
                    index += 4;
                }
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                return value.toString();
            }
            value.append(current);
        }
        return allowPartial && !escaped && !value.isEmpty() ? value.toString() : null;
    }

    private static int skipWhitespace(String value, int index) {
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }

    private static void appendEscaped(StringBuilder value, char escaped, String source, int index) {
        switch (escaped) {
            case 'n' -> value.append('\n');
            case 'r' -> value.append('\r');
            case 't' -> value.append('\t');
            case 'b' -> value.append('\b');
            case 'f' -> value.append('\f');
            case 'u' -> {
                if (index + 4 < source.length()) {
                    try {
                        value.append((char) Integer.parseInt(source.substring(index + 1, index + 5), 16));
                    } catch (NumberFormatException ignored) {
                        // 未完成或无效转义字段不生成草稿，等待后续完整 JSON 校验。
                    }
                }
            }
            default -> value.append(escaped);
        }
    }
}
