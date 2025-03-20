package com.c301.plugin.utils;

import com.c301.plugin.config.StoreCommitTemplateState;
import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.CommitTypeDomain;
import com.c301.plugin.model.LanguageDomain;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 通用工具类
 *
 * @Title CommUtil
 * @ClassName com.c301.plugin.utils.CommUtil
 * @Author Chenbing
 * @Date 25 /03/04 17:33
 * @Version 1.0
 */
public class CommUtil {

    private static final StoreCommitTemplateState store = StoreCommitTemplateState.getInstance();

    /**
     * 获取国际化资源
     *
     * @param languageKey 语言key
     * @return ResourceBundle resource bundle
     */
    public static ResourceBundle i18nResourceBundle(String languageKey) {
        if (StrUtil.isBlank(languageKey)) {
            languageKey = StoreCommitTemplateState.getInstance().getLanguage().getKey();
        }

        var locale = switch (languageKey) {
            case "zh_CN" -> Locale.SIMPLIFIED_CHINESE;
            case "zh_TW" -> Locale.TRADITIONAL_CHINESE;
            case "fr_FR" -> Locale.FRANCE;
            case "fr_CA" -> Locale.CANADA_FRENCH;
            case "de_DE" -> Locale.GERMANY;
            case "it_IT" -> Locale.ITALY;
            case "ja_JP" -> Locale.JAPAN;
            case "ko_KR" -> Locale.KOREA;
            default -> Locale.US;
        };
        return ResourceBundle.getBundle("i18n.data", locale);
    }

    /**
     * 获取语言实体
     *
     * @param languageKey the language key
     * @return language domain
     */
    public static LanguageDomain convertLanguageDomain(String languageKey) {
        return Constant.LANGUAGES.stream()
                .filter(item -> item.getKey().equals(languageKey))
                .findFirst()
                .orElse(LanguageDomain.EN_US);
    }

    /**
     * 获取选择器的语言实体
     *
     * @param optionLanguage the option language
     * @return language domain
     */
    public static LanguageDomain convertLanguageDomain(JComboBox<LanguageDomain> optionLanguage) {
        var language = (LanguageDomain) optionLanguage.getSelectedItem();
        if (language == null) language = LanguageDomain.EN_US;
        return language;
    }

    /**
     * 获取选择器的提交类型实体
     *
     * @param commitTypeButtonGroup 提交类型按钮组选中对象
     * @return 提交类型对象
     */
    public static CommitTypeDomain convertCommitTypeDomain(ButtonGroup commitTypeButtonGroup) {
        //加载自定义的提交类型
        if (store.isCustomEnable()) {


            return null;
        }

        //系统默认类型
        var commitTypeList = getDefaultCommitTypeList();
        var buttonElements = commitTypeButtonGroup.getElements();
        while (buttonElements.hasMoreElements()) {
            var button = buttonElements.nextElement();
            if (!button.isSelected()) continue;

            //选中对象
            var command = button.getActionCommand();
            for (CommitTypeDomain commitTypeDomain : commitTypeList) {
                if (commitTypeDomain.getType().equals(command)) {
                    return commitTypeDomain;
                }
            }
        }
        return commitTypeList.get(0);
    }

    /**
     * 传入一个类对象并深度拷贝返回一个拷贝对象
     *
     * @param obj 传入对象
     * @return 返回对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T obj) {
        //类型为List对象
        if (obj instanceof List<?> list) {
            var newList = new LinkedList<>();
            for (var o : list) newList.add(deepCopy(o));
            return (T) newList;
        }

        var gson = new Gson();
        var jsonString = gson.toJson(obj);
        return (T) gson.fromJson(jsonString, obj.getClass());
    }

    /**
     * 加载git提交变更范围历史
     *
     * @param project 项目对象
     * @return git提交历史列表
     */
    public static List<String> loadGitCommitScopeHistory(Project project) {
        var scopeList = new LinkedList<String>();

        try {
            ProcessBuilder processBuilder;
            var osName = System.getProperty("os.name");
            if (osName.contains("Windows")) processBuilder = new ProcessBuilder("cmd", "/C", Constant.GIT_LOG_COMMAND);
            else processBuilder = new ProcessBuilder("sh", "-c", Constant.GIT_LOG_COMMAND);

            var workingDirectory = new File(Objects.requireNonNull(project.getBasePath()));
            var process = processBuilder.directory(workingDirectory).start();
            var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            var historyList = reader.lines().toList();
            process.waitFor(2, TimeUnit.SECONDS);
            process.destroy();
            process.waitFor();

            //仅提取变更范围
            for (String history : historyList) {
                var matcher = Constant.COMMIT_FIRST_LINE_FORMAT.matcher(history);
                if (!matcher.find()) continue;

                var scope = matcher.group(3);
                if (StrUtil.isBlank(scope)) continue;
                scopeList.add(matcher.group(3));
            }
        } catch (Exception ignored) {
        }

        //添加空白占位
        scopeList.addFirst("");
        return scopeList;
    }

    /**
     * 加载默认的提交类型
     *
     * @return 提交类型对象列表
     */
    public static List<CommitTypeDomain> getDefaultCommitTypeList() {
        if (store.isCustomEnable()) return store.getCustomCommitTypeList();

        //默认提交类型列表
        return getDefaultCommitTypeList(null);
    }

    /**
     * 加载默认的提交类型
     *
     * @param languageKey 语言key
     * @return 提交类型对象列表
     */
    public static List<CommitTypeDomain> getDefaultCommitTypeList(String languageKey) {
        if (StrUtil.isBlank(languageKey)) languageKey = store.getLanguage().getKey();

        var commitTypeList = new LinkedList<CommitTypeDomain>();
        synchronized (CommitTypeDomain.class) {
            var resourceBundle = i18nResourceBundle(languageKey);
            for (String type : CommitTypeDomain.TYPES) {
                var description = resourceBundle.getString("plugin.radio." + type);
                if (StrUtil.isBlank(description)) description = type;
                commitTypeList.add(new CommitTypeDomain(type, description));
            }
        }
        return commitTypeList;
    }

    /**
     * 解析提交类型
     *
     * @param typeName 类型名称
     * @return 提交类型对象
     */
    public static CommitTypeDomain parseCommitType(String typeName) {
        var commitTypeList = getDefaultCommitTypeList();
        for (CommitTypeDomain commitType : commitTypeList) {
            if (commitType.getType().equals(typeName)) {
                return commitType;
            }
        }
        return null;
    }

}
