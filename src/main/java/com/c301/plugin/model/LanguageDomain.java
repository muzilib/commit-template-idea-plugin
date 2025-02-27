package com.c301.plugin.model;

/**
 * 显示语言信息
 *
 * @Title LanguageDomain
 * @ClassName com.c301.plugin.model.LanguageDomain
 * @Author Chenbing
 * @Date 25 /02/19 15:34
 * @Version 1.0
 */
public class LanguageDomain {

    public LanguageDomain(String key, String label) {
        this.key = key;
        this.label = label;
    }

    /**
     * 语言key
     */
    private final String key;
    /**
     * 显示名称
     */
    private final String label;

    /**
     * Gets key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

}
