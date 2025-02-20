package com.c301.plugin.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Git提交记录对象
 *
 * @Title GitCommitDomain
 * @ClassName com.c301.plugin.model.GitCommitDomain
 * @Author Chenbing
 * @Date 25 /02/19 17:43
 * @Version 1.0
 */
public class GitCommitDomain {

    /**
     * The constant ERROR.
     */
    public static GitCommitDomain ERROR = new GitCommitDomain();

    private int exitValue = -1;
    private List<String> logs = new ArrayList<>();

    private GitCommitDomain() {
    }

    /**
     * Instantiates a new Git commit domain.
     *
     * @param exitValue the exit value
     * @param logs      the logs
     */
    public GitCommitDomain(int exitValue, List<String> logs) {
        this.exitValue = exitValue;
        this.logs = logs;
    }


    /**
     * Is success boolean.
     *
     * @return the boolean
     */
    public boolean isSuccess() {
        return exitValue == 0;
    }

    /**
     * Gets logs.
     *
     * @return the logs
     */
    public List<String> getLogs() {
        return logs;
    }

}
