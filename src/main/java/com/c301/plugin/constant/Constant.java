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
    Pattern COMMIT_GITMOJI_FORMAT = Pattern.compile("^:[A-Za-z0-9_]+:");
    Pattern COMMIT_FIRST_LINE_FORMAT = Pattern.compile("^([a-z]+)(\\((.+)\\))?: (.+)");

    List<LanguageDomain> OPTINS_LANGUAGE_LIST = new LinkedList<>() {{
        //SIMPLIFIED_CHINESE 简体中文
        add(new LanguageDomain("zh_CN", "简体中文"));
        //TRADITIONAL_CHINESE 繁体中文
        add(new LanguageDomain("zh_TW", "繁体中文"));
        //FRANCE 法国
        add(new LanguageDomain("fr_FR", "法国"));
        //GERMANY 德国
        add(new LanguageDomain("de_DE", "德国"));
        //ITALY 意大利
        add(new LanguageDomain("it_IT", "意大利"));
        //JAPAN 日本
        add(new LanguageDomain("ja_JP", "日本"));
        //KOREA 韩国
        add(new LanguageDomain("ko_KR", "韩国"));
        //UK 英国
        add(new LanguageDomain("en_GB", "英国"));
        //US 美国
        add(new LanguageDomain("en_US", "美国"));
        //CANADA 加拿大
        add(new LanguageDomain("en_CA", "加拿大"));
        //CANADA_FRENCH 加拿大法语
        add(new LanguageDomain("fr_CA", "加拿大法语"));
    }};

}
