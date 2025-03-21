package com.c301.plugin.model;

import com.intellij.openapi.util.IconLoader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Gitmoji数据对象
 *
 * @Title GitmojiDomain
 * @ClassName com.c301.plugin.model.GitmojiDomain
 * @Author Chenbing
 * @Date 25/03/21 16:29
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitmojiDomain {
    /**
     * 系统默认的gitmoji数据
     */
    public static final List<GitmojiDomain> GITMOJIS = new LinkedList<>();

    private Icon icon;

    private String code;
    private String name;
    private String emoji;
    private String description;

    public GitmojiDomain(String code, String name, String emoji, String description) {
        this.code = code;
        this.name = name;
        this.emoji = emoji;
        this.description = description;
    }

    /**
     * 获取图标
     *
     * @return icon对象
     */
    public Icon getIcon() {
        if (icon == null) {
            try {
                var path = code.replaceAll(":", "");
                path = "/icons/gitmoji/" + path + ".png";
                icon = IconLoader.findIcon(path, GitmojiDomain.class, false, true);
            } catch (Exception e) {
                var path = "/icons/gitmoji/anguished.png";
                icon = IconLoader.findIcon(path, GitmojiDomain.class, false, true);
            }
        }
        return icon;
    }

}