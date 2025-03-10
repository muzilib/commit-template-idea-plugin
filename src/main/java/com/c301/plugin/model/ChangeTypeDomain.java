package com.c301.plugin.model;

/**
 * 提交类型域
 *
 * @Title ChangeTypeDomain
 * @ClassName com.c301.plugin.model.ChangeTypeDomain
 * @Author Chenbing
 * @Date 25 /03/05 10:38
 * @Version 1.0
 */
public class ChangeTypeDomain {

    /**
     * 类型名称
     */
    private String name;
    /**
     * 说明信息
     */
    private String direction;

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets direction.
     *
     * @return the direction
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Sets direction.
     *
     * @param direction the direction
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

}
