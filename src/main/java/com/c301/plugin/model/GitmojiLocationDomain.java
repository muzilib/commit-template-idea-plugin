package com.c301.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * Gitmoji表情位置
 *
 * @Title GitmojiLocationDomain
 * @ClassName com.c301.plugin.model.GitmojiLocationDomain
 * @Author Chenbing
 * @Date 25/03/28 08:44
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitmojiLocationDomain {

    public static GitmojiLocationDomain LOCATION1 = new GitmojiLocationDomain("location1");
    public static GitmojiLocationDomain LOCATION2 = new GitmojiLocationDomain("location2");
    public static GitmojiLocationDomain LOCATION3 = new GitmojiLocationDomain("location3");

    /**
     * 系统默认gitmoji位置数据
     */
    public static final List<GitmojiLocationDomain> LOCATIONS = new LinkedList<>() {{
        add(LOCATION1);
        add(LOCATION2);
        add(LOCATION3);
    }};

    /**
     * 位置Key
     */
    private String key;

    /**
     * 根据key获取GitmojiLocationDomain
     *
     * @param key 位置Key
     * @return 位置对象
     */
    public static GitmojiLocationDomain valueOf(String key) {
        return LOCATIONS.stream()
                .filter(location -> location.getKey().equals(key))
                .findFirst()
                .orElse(LOCATION1);
    }

}