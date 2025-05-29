package com.c301.plugin.config;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.PatchHunk;
import com.intellij.openapi.diff.impl.patch.TextFilePatch;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler;
import com.intellij.vcs.log.impl.TimedVcsCommitImpl;
import git4idea.GitCommit;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Title GitCommitAiWriteAction
 * @ClassName com.c301.plugin.config.GitCommitAiWriteAction
 * @Author Chenbing
 * @Date 25/05/28 16:30
 * @Version 1.0
 **/
@Slf4j
public class GitCommitAiWriteAction extends AnAction implements DumbAware {

    private static final Long MAX_PATCH_LEN = 70000L;
    private static final int MAX_SINGLE_LINE_LEN = 300;

    private Boolean checkIfChangeLengthTooLarge(List<FilePatch> patches, AtomicLong totalLength) {
        if (CollectionUtils.isEmpty(patches)) {
            return true;
        }

        Long lengthOfChange = 0L;

        for (FilePatch patch : patches) {
            if (!(patch instanceof TextFilePatch)) {
                continue;
            }

            List<PatchHunk> patchHunks = ((TextFilePatch) patch).getHunks();
            if (CollectionUtils.isEmpty(patchHunks)) {
                continue;
            }

            if (patchHunks.size() == 1) {
                PatchHunk patchHunk = patchHunks.get(0);
                if (patchHunk.getLines().size() == 1 && patchHunk.getText().length() > MAX_SINGLE_LINE_LEN) {
                    return false;
                }
            }

            for (PatchHunk patchHunk : patchHunks) {
                lengthOfChange += (long) patchHunk.getText().length();
            }
        }

        if (totalLength.get() + lengthOfChange > MAX_PATCH_LEN) {
            return false;
        } else {
            totalLength.addAndGet(lengthOfChange);
            return true;
        }
    }

    /*private List<String> getDiff(AnActionEvent anActionEvent) {
        Object workflowHandler = anActionEvent.getDataContext().getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
        if (workflowHandler == null) {
            return new ArrayList<>();
        } else {
            List<Change> changeList = new ArrayList<>();
            if (workflowHandler instanceof AbstractCommitWorkflowHandler) {
                List<Change> includedChanges = ((AbstractCommitWorkflowHandler) workflowHandler).getUi().getIncludedChanges();
                if (CollectionUtils.isNotEmpty(includedChanges)) {
                    changeList.addAll(includedChanges);
                }

                List<FilePath> filePaths = ((AbstractCommitWorkflowHandler) workflowHandler).getUi().getIncludedUnversionedFiles();
                if (CollectionUtils.isNotEmpty(filePaths)) {
                    for (FilePath filePath : filePaths) {
                        Change change = new Change(null, new CurrentContentRevision(filePath));
                        changeList.add(change);
                    }
                }

                includedChanges = new ArrayList();
                AtomicLong totalLength = new AtomicLong(0L);

                for (Change change : changeList) {
                    try {
                        Boolean isValid = this.checkIfValidChange(change);
                        if (BooleanUtils.isTrue(isValid)) {
                            List<FilePatch> patches = IdeaTextPatchBuilder.buildPatch(anActionEvent.getProject(), Arrays.asList(change), Path.of(anActionEvent.getProject().getBasePath()), false, false);
                            if (CollectionUtils.isEmpty(patches)) {
                                String fileName = change.getAfterRevision() != null ? change.getAfterRevision().getFile().getName() : (change.getBeforeRevision() != null ? change.getBeforeRevision().getFile().getName() : "");
                                if (!StringUtils.isBlank(fileName)) {
                                    includedChanges.add(fileName + " change mod");
                                    if (totalLength.get() >= MAX_PATCH_LEN || includedChanges.size() >= 50) {
                                        break;
                                    }
                                }
                            } else {
                                Boolean isValidChange = this.checkIfChangeLengthTooLarge(patches, totalLength);
                                if (BooleanUtils.isTrue(isValidChange)) {
                                    StringWriter writer = new StringWriter();

                                    try {
                                        UnifiedDiffWriter.write(anActionEvent.getProject(), ProjectKt.getStateStore(anActionEvent.getProject()).getProjectBasePath(), patches, writer, "\n", (CommitContext) null, List.of());
                                        if (StringUtils.isNotBlank(writer.toString())) {
                                            includedChanges.add(writer.toString());
                                        }

                                        if (includedChanges.size() >= 50 || totalLength.get() >= MAX_PATCH_LEN) {
                                            break;
                                        }
                                    } finally {
                                        writer.close();
                                    }
                                }
                            }
                        }
                    } catch (VcsException e) {
                        log.warn("get changeList error", e);
                    } catch (IOException e) {
                        log.warn("get changeList error", e);
                    }
                }

                return includedChanges;
            } else {
                return new ArrayList();
            }
        }
    }*/

    private Boolean checkIfValidChange(Change change) {
        Boolean isBinary = change.getAfterRevision() != null ? change.getAfterRevision().getFile().getFileType().isBinary() : change.getBeforeRevision().getFile().getFileType().isBinary();
        if (isBinary) {
            return false;
        } else {
            ContentRevision contentRevision = change.getAfterRevision() != null ? change.getAfterRevision() : change.getBeforeRevision();
            if (contentRevision == null) {
                return false;
            } else {
                String content = null;

                try {
                    content = contentRevision.getContent();
                } catch (VcsException e) {
                    log.warn("get content error", e);
                }

                return !StringUtils.isNotBlank(content) || content.contains("\n") || content.contains("\r") || content.length() <= 300;
            }
        }
    }

    public List<String> getDiff2(AnActionEvent anActionEvent) {
        var workflowHandler = anActionEvent.getDataContext().getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
        if (workflowHandler == null) return new ArrayList<>();

        var arrays = new ArrayList<String>();
        if (workflowHandler instanceof AbstractCommitWorkflowHandler) {
            var changeList = new ArrayList<Change>();
            var includedChanges = ((AbstractCommitWorkflowHandler) workflowHandler).getUi().getIncludedChanges();
            if (CollectionUtils.isNotEmpty(includedChanges)) {
                changeList.addAll(includedChanges);
            }

            List<FilePath> filePaths = ((AbstractCommitWorkflowHandler) anActionEvent.getDataContext().getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER)).getUi().getIncludedUnversionedFiles();
            if (CollectionUtils.isNotEmpty(filePaths)) {
                for (FilePath filePath : filePaths) {
                    Change change = new Change(null, new CurrentContentRevision(filePath));
                    changeList.add(change);
                }
            }

            for (Change change : changeList) {
                var isValid = this.checkIfValidChange(change);
                log.info("isValid is {}", isValid);
            }
            log.debug("filePaths is " + changeList.size());
        }
        return arrays;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        var aa = getDiff2(actionEvent);
        System.out.println(aa);

        var project = actionEvent.getProject();
        if (project == null) return;

        // 在后台线程中执行 Git 操作
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                var commitMessageList = readCommitMessageList(project);
                log.info("commitMessageList=>{}", commitMessageList.size());

                // 在 EDT 中更新 UI
                ApplicationManager.getApplication().invokeLater(() -> {
                    // 这里可以添加更新 UI 的代码
                    log.info("获取到 {} 条提交记录", commitMessageList.size());
                });
            } catch (Exception e) {
                log.error("获取提交记录失败", e);
            }
        });
    }

    private List<String> readCommitMessageList(Project project) {
        var commitMessageList = new ArrayList<String>();

        var projectFile = project.getProjectFile();
        if (projectFile == null) {
            var selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
            if (selectedFiles.length > 0) {
                projectFile = selectedFiles[0];
            }
        }

        GitRepository repository = null;
        if (projectFile != null) {
            repository = GitRepositoryManager.getInstance(project).getRepositoryForFile(projectFile);
        } else {
            var repositories = GitRepositoryManager.getInstance(project).getRepositories();
            if (CollectionUtils.isNotEmpty(repositories)) {
                repository = repositories.get(0);
            }
        }

        if (repository == null) {
            log.warn("No Git repository found for project");
            return commitMessageList;
        }

        try {
            var root = repository.getRoot();
            if (root == null) {
                log.warn("Repository root is null");
                return commitMessageList;
            }

            var commits = GitHistoryUtils.history(project, root, "--max-count=3");
            if (CollectionUtils.isEmpty(commits)) {
                return commitMessageList;
            }

            for (GitCommit commit : commits.stream()
                    .sorted(Comparator.comparing(TimedVcsCommitImpl::getTimestamp).reversed())
                    .limit(3L)
                    .toList()) {
                String commitMessage = commit.getFullMessage();
                if (StringUtils.isNotBlank(commitMessage)) {
                    commitMessageList.add(commitMessage);
                }
            }
        } catch (VcsException e) {
            log.error("获取 Git 历史记录失败", e);
        }

        return commitMessageList;
    }
}