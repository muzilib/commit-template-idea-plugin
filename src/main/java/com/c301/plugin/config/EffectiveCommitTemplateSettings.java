package com.c301.plugin.config;

import com.c301.plugin.domain.commit.CommitMessageRules;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitmojiLocationDomain;
import com.c301.plugin.model.LanguageDomain;

import java.util.List;

public record EffectiveCommitTemplateSettings(LanguageDomain language, boolean customEnable, boolean emojiEnable,
                                              GitmojiLocationDomain emojiLocation,
                                              List<CommitTypeDomain> customCommitTypeList,
                                              CommitMessageRules commitMessageRules,
                                              boolean previewEnabled) {
}
