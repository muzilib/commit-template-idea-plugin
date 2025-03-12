package com.c301.plugin.constant;

import com.c301.plugin.model.LanguageDomain;

import java.util.LinkedList;
import java.util.List;
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

    /**
     * 存储配置文件前缀
     */
    String ACTION_PREFIX = "$APP_CONFIG$/StoreCommitTemplateState";

    /**
     * 最大提交类型数量
     */
    int MAX_COMMIT_TYPE_LENGTH = 11;
    /**
     * 字段长度过长进行换行<br/>
     * <a href="https://stackoverflow.com/a/2120040/5138796">参考</a>
     */
    int MAX_LINE_LENGTH = 72;
    String CHAR_LINE = "\n";
    String STR_CLOSES = "Closes";
    String STR_BREAKING = "BREAKING";
    String STR_BREAKING_CHANGE = "BREAKING CHANGE: ";

    String SKIP_CI = "[skip ci]";
    String GIT_LOG_COMMAND = "git log --all --format=%s";

    Pattern COMMIT_CLOSES_FORMAT = Pattern.compile("Closes (.+)");
    Pattern COMMIT_FIRST_LINE_FORMAT = Pattern.compile("^([a-z]+)(\\((.+)\\))?: (.+)");


    /**
     * 语言列表
     */
    List<LanguageDomain> LANGUAGES = new LinkedList<>() {{
        add(LanguageDomain.EN_US);
        add(LanguageDomain.ZH_CN);
        add(LanguageDomain.ZH_TW);
        add(LanguageDomain.FR_FR);
        add(LanguageDomain.FR_CA);
        add(LanguageDomain.DE_DE);
        add(LanguageDomain.IT_IT);
        add(LanguageDomain.JA_JP);
        add(LanguageDomain.KO_KR);
    }};

}
