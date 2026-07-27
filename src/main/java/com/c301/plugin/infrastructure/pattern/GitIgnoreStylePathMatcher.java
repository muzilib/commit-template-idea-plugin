package com.c301.plugin.infrastructure.pattern;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.List;

/**
 * 面向 AI 传输的简化 .gitignore 风格匹配器。支持空行、注释、glob、目录规则和 ! 反向规则。
 * 强制敏感规则应在调用本类之前处理，不能被 ! 规则重新包含。
 */
public final class GitIgnoreStylePathMatcher {
    private GitIgnoreStylePathMatcher() {
    }

    public static boolean isExcluded(String relativePath, List<String> patterns) {
        String path = relativePath.replace('\\', '/');
        boolean excluded = false;
        if (patterns == null) {
            return false;
        }
        for (String raw : patterns) {
            if (raw == null || raw.isBlank() || raw.stripLeading().startsWith("#")) {
                continue;
            }
            String pattern = raw.trim();
            boolean include = pattern.startsWith("!");
            if (include) {
                pattern = pattern.substring(1);
            }
            if (matches(path, pattern)) {
                excluded = !include;
            }
        }
        return excluded;
    }

    private static boolean matches(String path, String pattern) {
        if (pattern.isBlank()) {
            return false;
        }
        String normalized = pattern.replace('\\', '/');
        if (normalized.endsWith("/")) {
            String directory = normalized.substring(0, normalized.length() - 1);
            return path.equals(directory) || path.startsWith(directory + "/")
                    || path.contains("/" + directory + "/");
        }
        String glob = normalized.contains("/") ? normalized : "**/" + normalized;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        return matcher.matches(java.nio.file.Path.of(path))
                || (!normalized.contains("/") && FileSystems.getDefault().getPathMatcher("glob:" + normalized)
                .matches(java.nio.file.Path.of(path)));
    }
}
