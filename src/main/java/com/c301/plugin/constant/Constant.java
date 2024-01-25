package com.c301.plugin.constant;

import java.util.regex.Pattern;

/**
 * 常量配置
 *
 * @Title Constant
 * @ClassName com.c301.plugin.constant.Constant
 * @Author Chenbing
 * @Date 2024/01/25 11:51
 * @Version 1.0
 **/
public interface Constant {

    String SKIP_CI = "[skip ci]";
    String GIT_LOG_COMMAND = "git log --all --format=%s";

    Pattern COMMIT_FIRST_LINE_FORMAT = Pattern.compile("^([a-z]+)(\\((.+)\\))?: (.+)");
    Pattern COMMIT_CLOSES_FORMAT = Pattern.compile("Closes (.+)");


}
