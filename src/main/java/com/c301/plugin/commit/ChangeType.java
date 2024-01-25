package com.c301.plugin.commit;

import com.c301.plugin.localization.PluginBundle;

/**
 * From <a href="https://github.com/commitizen/conventional-commit-types">Github</a>
 *
 * @author Damien Arrachequesne
 */
public enum ChangeType {

    FEAT(PluginBundle.get("plugin.commit.feat"), PluginBundle.get("plugin.commit.feat.desc")),
    FIX(PluginBundle.get("plugin.commit.fix"), PluginBundle.get("plugin.commit.fix.desc")),
    DOCS(PluginBundle.get("plugin.commit.docs"), PluginBundle.get("plugin.commit.docs.desc")),
    STYLE(PluginBundle.get("plugin.commit.style"), PluginBundle.get("plugin.commit.style.desc")),
    REFACTOR(PluginBundle.get("plugin.commit.refactor"), PluginBundle.get("plugin.commit.refactor.desc")),
    PERF(PluginBundle.get("plugin.commit.perf"), PluginBundle.get("plugin.commit.perf.desc")),
    TEST(PluginBundle.get("plugin.commit.test"), PluginBundle.get("plugin.commit.test.desc")),
    BUILD(PluginBundle.get("plugin.commit.build"), PluginBundle.get("plugin.commit.build.desc")),
    CI(PluginBundle.get("plugin.commit.ci"), PluginBundle.get("plugin.commit.ci.desc")),
    CHORE(PluginBundle.get("plugin.commit.chore"), PluginBundle.get("plugin.commit.chore.desc")),
    REVERT(PluginBundle.get("plugin.commit.revert"), PluginBundle.get("plugin.commit.revert.desc"));

    public final String title;
    public final String description;

    ChangeType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String label() {
        return this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.label(), this.description);
    }
}
