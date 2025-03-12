package com.c301.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 语言配置信息
 *
 * @Title LanguageDomain
 * @ClassName com.c301.plugin.model.LanguageDomain
 * @Author Chenbing
 * @Date 25 /03/11 17:31
 * @Version 1.0
 */
@Data
@AllArgsConstructor
public class LanguageDomain {

    private LanguageDomain() {
    }

    /**
     * en_US US 美国
     */
    public static final LanguageDomain EN_US = new LanguageDomain("en_US", "English");
    /**
     * zh_CN SIMPLIFIED_CHINESE 简体中文
     */
    public static final LanguageDomain ZH_CN = new LanguageDomain("zh_CN", "简体中文");
    /**
     * zh_TW TRADITIONAL_CHINESE 繁体中文
     */
    public static final LanguageDomain ZH_TW = new LanguageDomain("zh_TW", "繁體中文");
    /**
     * fr_FR FRANCE 法国
     */
    public static final LanguageDomain FR_FR = new LanguageDomain("fr_FR", "Français");
    /**
     * fr_CA CANADA_FRENCH 加拿大法语
     */
    public static final LanguageDomain FR_CA = new LanguageDomain("fr_CA", "Français canadien");
    /**
     * de_DE GERMANY 德国
     */
    public static final LanguageDomain DE_DE = new LanguageDomain("de_DE", "Deutsch");
    /**
     * it_IT ITALY 意大利
     */
    public static final LanguageDomain IT_IT = new LanguageDomain("it_IT", "Italiano");
    /**
     * ja_JP JAPAN 日本
     */
    public static final LanguageDomain JA_JP = new LanguageDomain("ja_JP", "日本語");
    /**
     * ko_KR KOREA 韩国
     */
    public static final LanguageDomain KO_KR = new LanguageDomain("ko_KR", "조선어");

    /**
     * 语言Key
     */
    private String key;
    /**
     * 显示文字
     */
    private String label;

}
