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
    /**
     * 存储语言的key
     */
    String STORE_LANGUAGE_KEY = "ctip_language_key";
    /**
     * 存储 窗口打开宽度 key
     */
    String STORE_WINDOW_WIDTH_KEY = "ctip_window_width_key";
    /**
     * 存储 窗口打开高度 key
     */
    String STORE_WINDOW_HEIGHT_KEY = "ctip_window_height_key";
    /**
     * 存储 窗口打开X坐标 key
     */
    String STORE_WINDOW_X_KEY = "ctip_window_x_key";
    /**
     * 存储 窗口打开Y坐标 key
     */
    String STORE_WINDOW_Y_KEY = "ctip_window_y_key";

    String CHAR_LINE = "\n";
    String STR_CLOSES = "Closes";
    String STR_BREAKING = "BREAKING";
    String STR_BREAKING_CHANGE = "BREAKING CHANGE: ";

    String SKIP_CI = "[skip ci]";
    String GIT_LOG_COMMAND = "git log --all --format=%s";

    Pattern COMMIT_CLOSES_FORMAT = Pattern.compile("Closes (.+)");
    Pattern COMMIT_FIRST_LINE_FORMAT = Pattern.compile("^([a-z]+)(\\((.+)\\))?: (.+)");

    //语言选项列表
    List<LanguageDomain> OPTINS_LANGUAGE_LIST = new LinkedList<>() {{
        //US 美国
        add(new LanguageDomain("en_US", "English"));
        //SIMPLIFIED_CHINESE 简体中文
        add(new LanguageDomain("zh_CN", "简体中文"));
        //TRADITIONAL_CHINESE 繁体中文
        add(new LanguageDomain("zh_TW", "繁體中文"));
        //FRANCE 法国
        add(new LanguageDomain("fr_FR", "Français"));
        //CANADA_FRENCH 加拿大法语
        add(new LanguageDomain("fr_CA", "Français canadien"));
        //GERMANY 德国
        add(new LanguageDomain("de_DE", "Deutsch"));
        //ITALY 意大利
        add(new LanguageDomain("it_IT", "Italiano"));
        //JAPAN 日本
        add(new LanguageDomain("ja_JP", "日本語"));
        //KOREA 韩国
        add(new LanguageDomain("ko_KR", "조선어"));
    }};

}
