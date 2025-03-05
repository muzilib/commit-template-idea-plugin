package com.c301.plugin.setting;

import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.StoreConfig;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 存储提交模板信息
 *
 * @Title StoreCommitTemplateState
 * @ClassName com.c301.plugin.setting.StoreCommitTemplateState
 * @Author Chenbing
 * @Date 25 /03/05 16:46
 * @Version 1.0
 */
@State(name = "StoreCommitTemplateState", storages = {@Storage(value = Constant.ACTION_PREFIX + "-settings.xml")})
public class StoreCommitTemplateState implements PersistentStateComponent<StoreCommitTemplateState> {

    /**
     * 默认值
     */
    public StoreConfig defaultValue = new StoreConfig();
    /**
     * 存储配置
     */
    public StoreConfig storeConfig = new StoreConfig();

    /**
     * Instantiates a new Store commit template state.
     */
    public StoreCommitTemplateState() {
    }

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

}
