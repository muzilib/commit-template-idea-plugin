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
     * 提交窗口 坐标X
     */
    public int commitWindowX = -1;
    /**
     * 提交窗口 坐标Y
     */
    public int commitWindowY = -1;
    /**
     * 提交窗口 宽
     */
    public int commitWindowWidth = -1;
    /**
     * 提交窗口 高
     */
    public int commitWindowHeight = -1;

    /**
     * 语言key
     */
    public String language = "en_US";
    /**
     * 是否启用模板
     */
    public boolean templateEnable = false;
    /**
     * 用户存储的模板
     */
    public List<ChangeTypeDomain> commitTypeList = new LinkedList<>();

}
