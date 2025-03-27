package com.c301.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 窗口配置对象
 *
 * @Title ConfigStoreDomain
 * @ClassName com.c301.plugin.model.ConfigStoreDomain
 * @Author Chenbing
 * @Date 25/03/11 17:38
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WindowsConfigDomain {

    /**
     * 窗口 X坐标
     */
    private int windowX = -1;
    /**
     * 窗口 Y坐标
     */
    private int windowY = -1;
    /**
     * 窗口 宽度
     */
    private int windowWidth = 0;
    /**
     * 窗口 高度
     */
    private int windowHeight = 0;

}
