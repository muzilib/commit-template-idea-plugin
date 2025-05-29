package com.c301.plugin.config;

import com.alibaba.fastjson.JSONObject;
import com.c301.plugin.utils.GitFileUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
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

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        var changeList = GitFileUtil.loadActiveGitChangeList(actionEvent);
        if (changeList.isEmpty()) {
            log.info("actionPerformed => changeList is empty");
            return;
        }

        var gitCommitChangeList = GitFileUtil.loadGitCommitFileChangeList(actionEvent, changeList);
        System.out.println(JSONObject.toJSONString(gitCommitChangeList));
        for (String gitCommitChange : gitCommitChangeList) {
            System.out.println("actionPerformed => " + gitCommitChange);
        }
    }

}