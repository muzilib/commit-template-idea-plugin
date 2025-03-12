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
    private String description;

    /**
     * 解析提交类型
     *
     * @param typeName 类型名称
     * @return 提交类型对象
     */
    public static CommitTypeDomain parseCommitType(String typeName) {
        for (String type : TYPES) {
            if (type.equalsIgnoreCase(typeName)) {
                return new CommitTypeDomain(typeName, typeName);
            }
        }
        return null;
    }

}
