package com.c301.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * 提交类型对象
 *
 * @Title CommitTypeDomain
 * @ClassName com.c301.plugin.model.CommitTypeDomain
 * @Author Chenbing
 * @Date 25/03/11 17:33
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitTypeDomain {

    /**
     * 最小化构造函数
     *
     * @param type  类型
     * @param emoji emoji文字
     */
    public CommitTypeDomain(String type, GitmojiDomain emoji) {
        this.type = type;
        this.emoji = emoji;
    }

    /**
     * 系统默认的11个提交类型
     */
    public static final List<String> TYPES = new LinkedList<>() {
        {
            add("feat");
            add("fix");
            add("docs");
            add("style");
            add("refactor");
            add("perf");
            add("test");
            add("build");
            add("ci");
            add("chore");
            add("revert");
        }
    };

    /**
     * 提交类型类型
     */
    private String type;
    /**
     * 提交类型说明
     */
    private GitmojiDomain emoji;
    /**
     * 提交类型说明
     */
    private String description;

    /**
     * 提交类型说明文字描述
     *
     * @param emojiEnable 是否开启emoji
     * @return 字符串
     */
    public String toString(boolean emojiEnable) {
        if (emojiEnable && emoji != null) {
            return emoji.getEmoji() + " " + type + " - " + description;
        }

        return type + " - " + description;
    }

    /**
     * 获取hashString字符串
     *
     * @return 字符串
     */
    public String hashString() {
        var value = type + " - " + description;
        if (emoji != null) {
            value += emoji.getCode();
        }
        return value;
    }

}
