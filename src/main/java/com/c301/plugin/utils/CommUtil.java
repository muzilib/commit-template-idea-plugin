package com.c301.plugin.utils;

import com.c301.plugin.config.StoreCommitTemplateState;
import com.c301.plugin.constant.Constant;
import com.c301.plugin.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    private static Properties properties = null;

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
        var store = StoreCommitTemplateState.getInstance();
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
        var store = StoreCommitTemplateState.getInstance();
        if (StrUtil.isBlank(languageKey)) languageKey = store.getLanguage().getKey();

        //读取gitmoji信息
        handleInitGitmojiEvent();
        var gitemojiMap = new HashMap<String, GitmojiDomain>();
        for (GitmojiDomain item : GitmojiDomain.GITMOJIS) gitemojiMap.put(item.getCode(), item);

        var commitTypeList = new LinkedList<CommitTypeDomain>();
        synchronized (CommitTypeDomain.class) {
            var resourceBundle = i18nResourceBundle(languageKey);
            for (String typeName : CommitTypeDomain.TYPES) {
                var description = resourceBundle.getString("plugin.radio." + typeName);
                if (StrUtil.isBlank(description)) description = typeName;

                //添加默认的gitmoji信息
                var gitmoji = switch (typeName) {
                    case "feat" -> gitemojiMap.get(":sparkles:");
                    case "fix" -> gitemojiMap.get(":bug:");
                    case "docs" -> gitemojiMap.get(":memo:");
                    case "style" -> gitemojiMap.get(":lipstick:");
                    case "refactor" -> gitemojiMap.get(":recycle:");
                    case "perf" -> gitemojiMap.get(":children_crossing:");
                    case "test" -> gitemojiMap.get(":white_check_mark:");
                    case "build" -> gitemojiMap.get(":building_construction:");
                    case "ci" -> gitemojiMap.get(":construction_worker:");
                    case "chore" -> gitemojiMap.get(":construction:");
                    case "revert" -> gitemojiMap.get(":rewind:");
                    default -> new GitmojiDomain("error", "error", "error", "error");
                };

                var commitType = new CommitTypeDomain();
                commitType.setType(typeName);
                commitType.setEmoji(gitmoji);
                commitType.setDescription(description);
                commitTypeList.add(commitType);
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

    /**
     * 初始化Gitmoji事件
     */
    public static void handleInitGitmojiEvent() {
        if (!GitmojiDomain.GITMOJIS.isEmpty()) return;

        try (var inputStream = CommUtil.class.getResourceAsStream("/icons/gitmojis.json")) {
            if (inputStream != null) {
                var json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                var listType = new TypeToken<LinkedList<GitmojiDomain>>() {
                }.getType();
                List<GitmojiDomain> list = new Gson().fromJson(json, listType);
                GitmojiDomain.GITMOJIS.addAll(list);
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * 读取gradle.properties文件中的值
     *
     * @param key 键
     * @return 值
     */
    public static String handleReadProperties(String key) {
        if (properties == null) {
            try (var inputStream = CommUtil.class.getResourceAsStream("/version.properties")) {
                if (inputStream != null) {
                    properties = new Properties();
                    properties.load(inputStream);
                }
            } catch (IOException ignored) {
            }
        }

        var value = properties.getProperty(key);
        if (StrUtil.isBlank(value)) return "-";
        return value;
    }

    /**
     * 获取gitmoji预览内容
     *
     * @param cache 变更缓存配置
     * @return 预览内容
     */
    public static String handlePreviewGitemojiLocation(SettingCacheDomain cache) {
        //读取gitmoji信息
        handleInitGitmojiEvent();
        var gitemojiMap = new HashMap<String, GitmojiDomain>();
        for (GitmojiDomain item : GitmojiDomain.GITMOJIS) gitemojiMap.put(item.getCode(), item);
        var gitmoji = gitemojiMap.get(":sparkles:");

        //设置提交类型、变更范围、短描述
        var resourceBundle = i18nResourceBundle(cache.getLanguage().getKey());
        var changeTypeText = resourceBundle.getString("plugin.label.typeOfChange");
        var changeScopeText = resourceBundle.getString("plugin.label.scopeOfThisChange");
        var shortDescriptionText = resourceBundle.getString("plugin.label.shortDescription");

        //构建预览内容
        var location = cache.getEmojiLocation();
        if (location.equals(GitmojiLocationDomain.LOCATION1)) {
            return gitmoji.getEmoji() + " " + changeTypeText + " (" + changeScopeText + "): " + shortDescriptionText;
        }
        if (location.equals(GitmojiLocationDomain.LOCATION2)) {
            return changeTypeText + " (" + gitmoji.getEmoji() + " " + changeScopeText + "): " + shortDescriptionText;
        }
        if (location.equals(GitmojiLocationDomain.LOCATION3)) {
            return changeTypeText + " (" + changeScopeText + "): " + gitmoji.getEmoji() + " " + shortDescriptionText;
        }
        return "Gitmoji";
    }

}
