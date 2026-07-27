package com.c301.plugin.config;


import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitmojiLocationDomain;
import com.c301.plugin.model.LanguageDomain;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

@Data
@NoArgsConstructor
@State(name = "ProjectCommitTemplateOverrideState", storages = @Storage("$PROJECT_CONFIG_DIR$/commit-template.xml"))
public class ProjectCommitTemplateOverrideState implements PersistentStateComponent<ProjectCommitTemplateOverrideState> {
    private LanguageDomain language;
    private Boolean customEnable;
    private Boolean emojiEnable;
    private GitmojiLocationDomain emojiLocation;
    private Boolean customCommitTypeListConfigured;
    private LinkedList<CommitTypeDomain> customCommitTypeList = new LinkedList<>();
    private Boolean requireCommitType;
    private Boolean requireScope;
    private Integer subjectMaxLength;
    private Integer bodyWrapLength;
    private String issueFooterKeyword;
    private Boolean forbidSubjectTrailingPeriod;

    public static ProjectCommitTemplateOverrideState getInstance(@NotNull Project project) {
        return project.getService(ProjectCommitTemplateOverrideState.class);
    }

    @Override
    public @Nullable ProjectCommitTemplateOverrideState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ProjectCommitTemplateOverrideState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void clearOverrides() {
        language = null;
        customEnable = null;
        emojiEnable = null;
        emojiLocation = null;
        customCommitTypeListConfigured = null;
        customCommitTypeList.clear();
        requireCommitType = null;
        requireScope = null;
        subjectMaxLength = null;
        bodyWrapLength = null;
        issueFooterKeyword = null;
        forbidSubjectTrailingPeriod = null;
    }
}
