package com.c301.plugin.model;

import com.c301.plugin.utils.CommUtil;
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
    public static final List<CommitTypeDomain> TYPES = new LinkedList<>() {
        {
            CommUtil.handleInitGitmojiEvent();

            add(new CommitTypeDomain("feat", null));
            add(new CommitTypeDomain("fix", null));
            add(new CommitTypeDomain("docs", null));
            add(new CommitTypeDomain("style", null));
            add(new CommitTypeDomain("refactor", null));
            add(new CommitTypeDomain("perf", null));
            add(new CommitTypeDomain("test", null));
            add(new CommitTypeDomain("build", null));
            add(new CommitTypeDomain("ci", null));
            add(new CommitTypeDomain("chore", null));
            add(new CommitTypeDomain("revert", null));
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
            return emoji.getCode() + " " + type + " - " + description;
        }

        return type + " - " + description;
    }

}
