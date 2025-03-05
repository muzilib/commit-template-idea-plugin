package com.c301.plugin.model;

import java.util.LinkedList;
import java.util.List;

/**
 * 存储配置类对象
 *
 * @Title StoreConfig
 * @ClassName com.c301.plugin.model.StoreConfig
 * @Author Chenbing
 * @Date 25 /03/05 16:51
 * @Version 1.0
 */
public class StoreConfig {

    /**
     * 语言key
     */
    private String language = "en_US";
    /**
     * 是否启用模板
     */
    private boolean templateEnable = false;
    /**
     * 用户存储的模板
     */
    private List<ChangeTypeDomain> commitTypeList = new LinkedList<>();

    /**
     * Gets language.
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets language.
     *
     * @param language the language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets commit type list.
     *
     * @return the commit type list
     */
    public List<ChangeTypeDomain> getCommitTypeList() {
        return commitTypeList;
    }

    /**
     * Sets commit type list.
     *
     * @param commitTypeList the commit type list
     */
    public void setCommitTypeList(List<ChangeTypeDomain> commitTypeList) {
        this.commitTypeList = commitTypeList;
    }

    /**
     * Is template enable boolean.
     *
     * @return the boolean
     */
    public boolean isTemplateEnable() {
        return templateEnable;
    }

    /**
     * Sets template enable.
     *
     * @param templateEnable the template enable
     */
    public void setTemplateEnable(boolean templateEnable) {
        this.templateEnable = templateEnable;
    }

}
