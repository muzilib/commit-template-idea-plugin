package com.c301.plugin.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.project.ProjectKt;
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Git 文件工具类
 *
 * @Title GitFileUtil
 * @ClassName com.c301.plugin.utils.GitFileUtil
 * @Author Chenbing
 * @Date 25/05/29 12:02
 * @Version 1.0
 **/
@Slf4j
public class GitFileUtil {

    /**
     * 获取git文件修改列表
     *
     * @param anActionEvent 事件
     * @return 变更文件列表
     */
    public static List<Change> loadActiveGitChangeList(AnActionEvent anActionEvent) {
        var changeList = new ArrayList<Change>();

        var workflowHandler = anActionEvent.getDataContext().getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
        if (workflowHandler == null) return changeList;
        if (!(workflowHandler instanceof AbstractCommitWorkflowHandler)) return changeList;

        //获取选中文件列表
        var includedChanges = ((AbstractCommitWorkflowHandler<?, ?>) workflowHandler).getUi().getIncludedChanges();
        if (!includedChanges.isEmpty()) changeList.addAll(includedChanges);

        //读取未添加到Git仓库的文件
        var filePaths = ((AbstractCommitWorkflowHandler<?, ?>) Objects.requireNonNull(anActionEvent.getDataContext().getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER))).getUi().getIncludedUnversionedFiles();
        if (CollectionUtils.isNotEmpty(filePaths)) {
            for (var filePath : filePaths) {
                changeList.add(new Change(null, new CurrentContentRevision(filePath)));
            }
        }

        return changeList;
    }

    /**
     * 获取git文件修改列表
     *
     * @param anActionEvent 事件
     * @param changeList    变更文件列表
     * @return 变更文件列表
     */
    public static List<String> loadGitCommitFileChangeList(AnActionEvent anActionEvent, List<Change> changeList) {
        var project = anActionEvent.getProject();
        var commitFileChangeList = new ArrayList<String>();
        if (project == null) return commitFileChangeList;

        for (Change change : changeList) {
            try {
                var isValidChange = handleCheckValidChange(change);
                if (!isValidChange) continue;

                //生成补丁信息
                var patches = IdeaTextPatchBuilder.buildPatch(project, List.of(change),
                        Path.of(Objects.requireNonNull(project.getBasePath())),
                        false, false);
                if (CollectionUtils.isEmpty(patches)) {
                    // 仅记录文件名称的变更
                    var fileName = handleReadFilename(change);
                    if (StrUtil.isNotBlank(fileName)) {
                        commitFileChangeList.add(fileName + " change mod");
                    }
                } else {
                    //生成文件详细变更信息
                    try (var writer = new StringWriter()) {
                        UnifiedDiffWriter.write(project, ProjectKt.getStateStore(project).getProjectBasePath(),
                                patches, writer, "\n", null, List.of());
                        var content = writer.toString();
                        if (StrUtil.isNotBlank(content)) {
                            commitFileChangeList.add(content);
                        }
                    }
                }
            } catch (VcsException | IOException e) {
                log.warn("loadGitCommitFileChangeList error", e);
            }
        }

        return commitFileChangeList;
    }

    /**
     * 判断文件变更是否有效
     *
     * @param change 变更信息
     * @return true: 文件变更有效
     */
    private static Boolean handleCheckValidChange(Change change) {
        //无效文件信息
        if (change.getAfterRevision() == null && change.getBeforeRevision() == null) return false;
        var afterRevision = change.getAfterRevision();
        var beforeRevision = change.getBeforeRevision();

        //比对文件是否为二进制文件
        if (afterRevision != null) {
            var afterIsBinary = afterRevision.getFile().getFileType().isBinary();
            if (afterIsBinary) return false;
        }
        if (beforeRevision != null) {
            var beforeIsBinary = beforeRevision.getFile().getFileType().isBinary();
            if (beforeIsBinary) return false;
        }

        var contentRevision = afterRevision == null ? beforeRevision : afterRevision;
        try {
            var content = contentRevision.getContent();

            // 文档长度大于500、不包含换行符、内容为空都跳过
            return !StrUtil.isNotBlank(content) ||
                    content.contains("\n") ||
                    content.contains("\r") ||
                    content.length() <= 300;
        } catch (VcsException e) {
            log.warn("get content error", e);
        }
        return false;
    }

    /**
     * 读取文件名称
     *
     * @param change 文件变更信息
     * @return 文件名称
     */
    private static String handleReadFilename(Change change) {
        //读取当前文件名称
        var afterRevision = change.getAfterRevision();
        if (afterRevision != null) {
            return afterRevision.getFile().getName();
        }

        //读取历史文件名称
        var beforeRevision = change.getBeforeRevision();
        if (beforeRevision != null) {
            return beforeRevision.getFile().getName();
        }
        return null;
    }

//    系统提示词模板
    //### **Git 提交日志生成助手**
    //
    //#### **任务描述**
    //根据您提供的变更文件的补丁信息和相关文件内容，我将为您生成符合以下格式的 Git 提交记录模板：
    //
    //```
    //<类型>(<范围>): <简短描述>
    //
    //<详细描述>
    //- 详细描述点 1
    //- 详细描述点 2
    //```
    //
    //#### **字段说明**
    //1. **类型**
    //   描述提交的主要类别，选择以下之一：
    //   - `feat`: 新功能 (feature)
    //   - `fix`: 修复问题 (bug fix)
    //   - `docs`: 文档更新 (documentation)
    //   - `style`: 代码格式调整 (不影响代码逻辑)
    //   - `refactor`: 重构代码 (既不新增功能也不修复问题)
    //   - `test`: 添加或修改测试
    //   - `chore`: 构建或工具变更
    //   - `perf`: 性能优化
    //
    //2. **范围**
    //   描述提交影响的模块或文件范围，例如：`auth`, `user`, `api`, `frontend`, `backend` 等。
    //
    //3. **简短描述**
    //   用一句话概括本次提交的核心内容，尽量控制在50个字符以内。
    //
    //4. **详细描述**
    //   格式为无序列表，每条描述前加 `-`。
    //   对提交内容的详细说明，解释为什么做这个变更以及如何实现的。
    //   如果变更较简单，可以省略此部分。
    //   所有内容必须要简短明确的说明，不要过多无意义的描述
    //
    //##### 示例 1: 输出
    //```
    //fix(auth): 修复登录空值校验缺失问题
    //
    //- 增加了对用户名和密码为空的校验
    //- 抛出错误提示以防止非法输入
    //```
    //
    //##### 示例 2: 输出
    //```
    //docs(readme): 补充环境变量配置说明
    //
    //- 在安装步骤中添加了 `.env` 文件配置说明
    //- 提高了文档的完整性
    //```


//    用户提示词
    //请基于提供的变更补丁列表数据，生成标准的Git提交模板信息
    //生成的Git信息合并为一条提交记录
    //以下是列表数据
    //
    //[ "Index: Dockerfile\n===================================================================\ndiff --git a/Dockerfile b/Dockerfile\n--- a/Dockerfile\t(revision b9ed94651a61c2eb9355a8a699c4bdd017a7c42f)\n+++ b/Dockerfile\t(date 1748497622069)\n@@ -3,7 +3,8 @@\n LABEL maintainer = Chenbing\n \n #按需求调整\n-EXPOSE 8080\n+EXPOSE 8081\n+EXPOSE 8082\n ENV APPNAME *.jar\n \n #默认的工作目录\n", "Index: README.md\n===================================================================\ndiff --git a/README.md b/README.md\n--- a/README.md\t(revision b9ed94651a61c2eb9355a8a699c4bdd017a7c42f)\n+++ b/README.md\t(date 1748497622061)\n@@ -2,6 +2,12 @@\n \n Eos 网关项目\n \n+你好\n+\n+Hello\n+\n+word\n+\n ## 立即开始\n \n ```bash\n" ]
    //
    //以上是列表数据
    //返回信息请严格参考模板格式，并确保每个字段的格式和内容正确。


}