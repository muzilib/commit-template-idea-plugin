package com.c301.plugin.config;

import com.c301.plugin.constant.Constant;

import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.GitmojiLocationDomain;
import com.c301.plugin.model.LanguageDomain;
import com.c301.plugin.model.WindowsConfigDomain;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * 存储提交模板信息
 *
 * @Title StoreCommitTemplateState
 * @ClassName com.c301.plugin.config.StoreCommitTemplateState
 * @Author Chenbing
 * @Date 25 /03/05 16:46
 * @Version 1.0
 */
@Accessors(chain = true)
@Data
@NoArgsConstructor
@State(name = "StoreCommitTemplateState", storages = {@Storage(value = Constant.ACTION_PREFIX + "-settings.xml")})
public class StoreCommitTemplateState implements PersistentStateComponent<StoreCommitTemplateState> {

    /**
     * 提交窗口的配置信息
     */
    private WindowsConfigDomain commitWindowConfig = null;
    /**
     * 编辑提交类型窗口的配置信息
     */
    private WindowsConfigDomain settingCommitTypeWindowConfig = null;
    /**
     * 语言配置信息
     */
    private LanguageDomain language = LanguageDomain.EN_US;
    /**
     * 插件界面文案是否跟随 IntelliJ IDEA 的界面语言环境。
     */
    private boolean syncUiLanguageWithIde = true;
    /**
     * 兼容字段：用于读取改名为“同步 IDEA 界面语言”之前保存的旧配置。
     */
    @Deprecated
    private Boolean syncUiLanguageWithOs;
    /**
     * 关闭与 IntelliJ IDEA 同步时，插件界面使用的语言。
     */
    private LanguageDomain uiLanguage = LanguageDomain.EN_US;
    /**
     * 是否在提交弹窗中显示生成后的提交信息预览，作为全局默认值。
     */
    private boolean previewEnabled = true;
    /**
     * 自定义模板启用状态
     */
    private boolean customEnable = false;
    /**
     * 自定义emoji启用状态
     */
    private boolean emojiEnable = false;
    /**
     * 自定义emoji位置
     */
    private GitmojiLocationDomain emojiLocation = GitmojiLocationDomain.LOCATION1;
    /**
     * 用户自定义 提交类型列表
     */
    private LinkedList<CommitTypeDomain> customCommitTypeList = new LinkedList<>();
    /**
     * 提交信息校验与格式化规则的全局默认配置。
     */
    private CommitMessageRulesState commitMessageRules = new CommitMessageRulesState();

    /**
     * 对象访问构造器
     *
     * @return instance instance
     */
    public static StoreCommitTemplateState getInstance() {
        return ApplicationManager.getApplication().getService(StoreCommitTemplateState.class);
    }

    @Override
    public @Nullable StoreCommitTemplateState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull StoreCommitTemplateState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean isSyncUiLanguageWithIde() {
        return syncUiLanguageWithOs != null ? syncUiLanguageWithOs : syncUiLanguageWithIde;
    }

    public void setSyncUiLanguageWithIde(boolean syncUiLanguageWithIde) {
        this.syncUiLanguageWithIde = syncUiLanguageWithIde;
        this.syncUiLanguageWithOs = null;
    }

}
