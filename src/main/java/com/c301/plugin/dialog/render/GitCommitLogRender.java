package com.c301.plugin.dialog.render;

import com.c301.plugin.model.GitCommitDomain;
import com.intellij.openapi.project.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.c301.plugin.constant.Constant.GIT_LOG_COMMAND;
import static java.util.stream.Collectors.toList;

/**
 * Git提交日志查询
 *
 * @Title GitCommitLogRender
 * @ClassName com.c301.plugin.dialog.render.GitCommitLogRender
 * @Author Chenbing
 * @Date 25/02/19 17:42
 * @Version 1.0
 **/
public class GitCommitLogRender {
    private static final Pattern COMMIT_FIRST_LINE_FORMAT = Pattern.compile("^[a-z]+\\((.+)\\):.*");

    public static GitCommitDomain handleCommitHistory(Project project) {
        try {
            ProcessBuilder processBuilder;
            var osName = System.getProperty("os.name");
            if (osName.contains("Windows")) processBuilder = new ProcessBuilder("cmd", "/C", GIT_LOG_COMMAND);
            else processBuilder = new ProcessBuilder("sh", "-c", GIT_LOG_COMMAND);

            var workingDirectory = new File(Objects.requireNonNull(project.getBasePath()));
            var process = processBuilder.directory(workingDirectory).start();
            var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            var output = reader.lines().collect(toList());

            process.waitFor(2, TimeUnit.SECONDS);
            process.destroy();
            process.waitFor();
            return new GitCommitDomain(0, output);
        } catch (Exception ignored) {
        }
        return GitCommitDomain.ERROR;
    }

}
