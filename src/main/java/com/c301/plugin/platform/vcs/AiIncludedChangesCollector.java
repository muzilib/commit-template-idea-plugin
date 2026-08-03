package com.c301.plugin.platform.vcs;

import com.c301.plugin.config.AiPreferencesState;

import com.c301.plugin.infrastructure.pattern.GitIgnoreStylePathMatcher;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.project.ProjectKt;
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 仅采集 IDEA 当前 Commit UI 已勾选的变更，并在读取内容前应用路径和二进制过滤。
 */
public final class AiIncludedChangesCollector {
    private static final int MAX_METADATA_FILES = 100;
    private static final int MAX_DIFF_CHARACTERS = 80_000;

    private AiIncludedChangesCollector() {
    }

    public static CollectionResult collectMetadata(AnActionEvent event, AiPreferencesState preferences) {
        var handler = event.getDataContext().getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
        if (!(handler instanceof AbstractCommitWorkflowHandler<?, ?> workflow)) {
            return new CollectionResult(List.of(), List.of("无法读取当前 Commit 窗口的已包含变更。"), List.of());
        }
        List<String> included = new ArrayList<>();
        List<String> excluded = new ArrayList<>();
        List<Change> acceptedChanges = new ArrayList<>();
        for (Change change : workflow.getUi().getIncludedChanges()) {
            if (included.size() >= MAX_METADATA_FILES) {
                excluded.add("其余文件：超过最大文件数量限制");
                break;
            }
            String path = pathOf(event.getProject(), change);
            if (path == null) {
                excluded.add("未知文件：无法获取路径");
                continue;
            }
            if (isBinary(change)) {
                excluded.add(path + "：二进制文件");
                continue;
            }
            if (isForcedSensitive(path)) {
                excluded.add(path + "：内置敏感规则");
                continue;
            }
            if (GitIgnoreStylePathMatcher.isExcluded(path, preferences.getExcludePatterns())) {
                excluded.add(path + "：用户排除规则");
                continue;
            }
            included.add(changeType(change) + " " + path);
            acceptedChanges.add(change);
        }
        return new CollectionResult(included, excluded, acceptedChanges);
    }

    /**
     * 发送给远程服务的路径必须相对项目根目录，避免泄露本机用户名或目录结构。
     */
    private static String pathOf(Project project, Change change) {
        var revision = change.getAfterRevision() != null ? change.getAfterRevision() : change.getBeforeRevision();
        if (revision == null) {
            return null;
        }
        String absolutePath = revision.getFile().getPath().replace('\\', '/');
        if (project == null || project.getBasePath() == null) {
            return revision.getFile().getName();
        }
        try {
            Path base = Path.of(project.getBasePath()).toAbsolutePath().normalize();
            Path file = Path.of(absolutePath).toAbsolutePath().normalize();
            if (!file.startsWith(base)) {
                return null;
            }
            return base.relativize(file).toString().replace('\\', '/');
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static boolean isBinary(Change change) {
        return (change.getAfterRevision() != null && change.getAfterRevision().getFile().getFileType().isBinary())
                || (change.getBeforeRevision() != null && change.getBeforeRevision().getFile().getFileType().isBinary());
    }

    private static boolean isForcedSensitive(String path) {
        String lower = path.toLowerCase();
        String name = lower.substring(lower.lastIndexOf('/') + 1);
        return name.equals(".env") || name.startsWith(".env.") || name.endsWith(".pem") || name.endsWith(".key")
                || name.equals("id_rsa") || name.equals("id_ed25519") || name.contains("credential")
                || name.contains("secret") || name.contains("password") || name.contains("token");
    }

    private static String changeType(Change change) {
        if (change.getBeforeRevision() == null) {
            return "ADDED";
        }
        if (change.getAfterRevision() == null) {
            return "DELETED";
        }
        return "MODIFIED";
    }

    /**
     * 在用户确认发送 Diff 后才调用。Diff 不落盘，并在达到总字符限制时停止继续加入文件。
     */
    public static DiffCollectionResult collectDiff(Project project, CollectionResult result,
                                                   ProgressIndicator indicator) {
        StringBuilder allDiffs = new StringBuilder();
        List<String> excluded = new ArrayList<>();
        int includedFileCount = 0;
        boolean truncated = false;
        for (int index = 0; index < result.acceptedChanges().size(); index++) {
            ProgressManager.checkCanceled();
            if (indicator != null && indicator.isCanceled()) {
                throw new com.intellij.openapi.progress.ProcessCanceledException();
            }
            Change change = result.acceptedChanges().get(index);
            String path = pathOf(project, change);
            try {
                var patches = IdeaTextPatchBuilder.buildPatch(project, List.of(change),
                        Path.of(project.getBasePath()), false, false);
                if (patches.isEmpty()) {
                    excluded.add((path == null ? "未知文件" : path) + "：无法生成文本 Diff");
                    continue;
                }
                try (StringWriter writer = new StringWriter()) {
                    UnifiedDiffWriter.write(project, ProjectKt.getStateStore(project).getProjectBasePath(),
                            patches, writer, "\n", null, List.of());
                    String diff = writer.toString();
                    if (allDiffs.length() + diff.length() > MAX_DIFF_CHARACTERS) {
                        truncated = true;
                        excluded.add("其余 " + (result.acceptedChanges().size() - index) + " 个文件：超过 Diff 总字符限制");
                        break;
                    }
                    allDiffs.append(diff);
                    includedFileCount++;
                }
            } catch (VcsException | IOException | RuntimeException ignored) {
                // 单个文件无法生成 Diff 时跳过，避免失败文件阻断其余已确认变更。
                excluded.add((path == null ? "未知文件" : path) + "：无法生成 Diff");
            }
        }
        return new DiffCollectionResult(allDiffs.toString(), includedFileCount, allDiffs.length(), truncated, excluded);
    }

    public record DiffCollectionResult(String diff, int includedFileCount, int characterCount,
                                       boolean truncated, List<String> excludedChanges) {
    }

    public record CollectionResult(List<String> includedMetadata, List<String> excludedChanges,
                                   List<Change> acceptedChanges) {
        public String asPromptContent() {
            return String.join("\n", includedMetadata);
        }
    }
}
