package com.c301.plugin.config;

import com.c301.plugin.domain.commit.CommitMessageRules;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitmojiLocationDomain;
import com.c301.plugin.model.LanguageDomain;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class CommitTemplateSettingsResolver {
    private final Project project;

    public CommitTemplateSettingsResolver(@NotNull Project project) {
        this.project = project;
    }

    public static CommitTemplateSettingsResolver getInstance(@NotNull Project project) {
        return project.getService(CommitTemplateSettingsResolver.class);
    }

    public @NotNull EffectiveCommitTemplateSettings resolve() {
        StoreCommitTemplateState global = StoreCommitTemplateState.getInstance();
        ProjectCommitTemplateOverrideState override = ProjectCommitTemplateOverrideState.getInstance(project);

        LanguageDomain language = override.getLanguage() != null ? override.getLanguage() : global.getLanguage();
        boolean customEnable = override.getCustomEnable() != null ? override.getCustomEnable() : global.isCustomEnable();
        boolean emojiEnable = override.getEmojiEnable() != null ? override.getEmojiEnable() : global.isEmojiEnable();
        GitmojiLocationDomain emojiLocation = override.getEmojiLocation() != null ? override.getEmojiLocation() : global.getEmojiLocation();
        List<CommitTypeDomain> commitTypes = Boolean.TRUE.equals(override.getCustomCommitTypeListConfigured())
                ? override.getCustomCommitTypeList()
                : global.getCustomCommitTypeList();
        CommitMessageRules globalRules = global.getCommitMessageRules() == null
                ? CommitMessageRules.defaults()
                : global.getCommitMessageRules().toDomain();
        CommitMessageRules rules = new CommitMessageRules(
                override.getRequireCommitType() == null ? globalRules.requireCommitType() : override.getRequireCommitType(),
                override.getRequireScope() == null ? globalRules.requireScope() : override.getRequireScope(),
                override.getSubjectMaxLength() == null ? globalRules.subjectMaxLength() : override.getSubjectMaxLength(),
                override.getBodyWrapLength() == null ? globalRules.bodyWrapLength() : override.getBodyWrapLength(),
                override.getIssueFooterKeyword() == null ? globalRules.issueFooterKeyword() : override.getIssueFooterKeyword(),
                override.getForbidSubjectTrailingPeriod() == null
                        ? globalRules.forbidSubjectTrailingPeriod()
                        : override.getForbidSubjectTrailingPeriod()
        );

        return new EffectiveCommitTemplateSettings(
                language == null ? LanguageDomain.EN_US : language,
                customEnable,
                emojiEnable,
                emojiLocation == null ? GitmojiLocationDomain.LOCATION1 : emojiLocation,
                copyCommitTypes(commitTypes),
                rules
        );
    }

    private static List<CommitTypeDomain> copyCommitTypes(List<CommitTypeDomain> source) {
        List<CommitTypeDomain> copy = new LinkedList<>();
        if (source == null) {
            return copy;
        }
        for (CommitTypeDomain commitType : source) {
            if (commitType == null) {
                continue;
            }
            copy.add(new CommitTypeDomain(commitType.getType(), commitType.getEmoji(), commitType.getDescription()));
        }
        return copy;
    }
}
