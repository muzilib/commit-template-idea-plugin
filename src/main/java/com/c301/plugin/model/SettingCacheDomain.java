package com.c301.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

/**
 * 设置缓存对象，做存储检查功能
 *
 * @Title SettingCacheDomain
 * @ClassName com.c301.plugin.model.SettingCacheDomain
 * @Author Chenbing
 * @Date 25/03/20 16:51
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingCacheDomain {

    /**
     * 语言配置信息
     */
    private LanguageDomain language = LanguageDomain.EN_US;
    /**
     * 自定义模板启用状态
     */
    private boolean customEnable = false;
    /**
     * 自定义emoji启用状态
     */
    private boolean emojiEnable = false;
    /**
     * 用户自定义 提交类型列表
     */
    private LinkedList<CommitTypeDomain> customCommitTypeList = new LinkedList<>();

}