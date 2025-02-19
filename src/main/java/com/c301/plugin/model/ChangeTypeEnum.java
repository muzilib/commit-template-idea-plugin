package com.c301.plugin.model;

/**
 * 提交类型枚举
 * From <a href="https://github.com/commitizen/conventional-commit-types">Github</a>
 *
 * @Title ChangeTypeDomain
 * @ClassName com.c301.plugin.model.ChangeTypeDomain
 * @Author Damien Arrachequesne Chenbing
 * @Date 25 /02/19 16:34
 * @Version 1.0
 */
public enum ChangeTypeEnum {

    /**
     * A new feature
     */
    FEAT("Features"),
    /**
     * A bug fix
     */
    FIX("Bug Fixes"),
    /**
     * Documentation only changes
     */
    DOCS("Documentation"),
    /**
     * Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
     */
    STYLE("Styles"),
    /**
     * A code change that neither fixes a bug nor adds a feature
     */
    REFACTOR("Code Refactoring"),
    /**
     * A code change that improves performance
     */
    PERF("Performance Improvements"),
    /**
     * Adding missing tests or correcting existing tests
     */
    TEST("Tests"),
    /**
     * Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)
     */
    BUILD("Builds"),
    /**
     * Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)
     */
    CI("Continuous Integrations"),
    /**
     * Other changes that don't modify src or test files
     */
    CHORE("Chores"),
    /**
     * Reverts a previous commit
     */
    REVERT("Reverts");

    private final String name;

    /**
     * 获取选项唯一的code
     *
     * @return the code
     */
    public String getCode() {
        return this.name().toLowerCase();
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    ChangeTypeEnum(String name) {
        this.name = name;
    }

}
