package com.c301.plugin.config;

import com.c301.plugin.utils.GitFileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * @Title GitCommitAiWriteAction
 * @ClassName com.c301.plugin.config.GitCommitAiWriteAction
 * @Author Chenbing
 * @Date 25/05/28 16:30
 * @Version 1.0
 **/
@Slf4j
public class GitCommitAiWriteAction extends AnAction implements DumbAware {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        var changeList = GitFileUtil.loadActiveGitChangeList(actionEvent);
        if (changeList.isEmpty()) {
            log.info("actionPerformed => changeList is empty");
            return;
        }

        //弹窗提示信息

        var gitCommitChangeList = GitFileUtil.loadGitCommitFileChangeList(actionEvent, changeList);
        try {
            System.out.println(objectMapper.writeValueAsString(gitCommitChangeList));
        } catch (Exception e) {
            log.error("Failed to serialize gitCommitChangeList", e);
        }
        for (String gitCommitChange : gitCommitChangeList) {
            System.out.println("actionPerformed => " + gitCommitChange);
        }

        // project 可以为 null，如果没有特定项目
        Project project = actionEvent.getProject();
        NotificationGroupManager.getInstance()
                .getNotificationGroup("commit-template-notify")
                .createNotification("通义灵码", "没有文件变更，或所选择的文件不符合条件", NotificationType.INFORMATION)
                .notify(project);
    }

}