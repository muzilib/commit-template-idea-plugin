package com.c301.plugin.commit;

import java.util.LinkedList;
import java.util.Map;

/**
 * Gitmoji 数据列表
 *
 * @Title GitmojiType
 * @ClassName com.c301.plugin.commit.GitmojiType
 * @Author Chenbing
 * @Date 24/06/30 20,29
 * @Version 1.0
 **/
public class GitmojiType {

    private static final LinkedList<Map<String, String>> DATA_LIST = new LinkedList<>();

    static {
        DATA_LIST.push(Map.of("icon", "🎨", "code", ":art:", "name", "art"));
        DATA_LIST.push(Map.of("icon", "⚡️", "code", ":zap:", "name", "zap"));
        DATA_LIST.push(Map.of("icon", "🔥", "code", ":fire:", "name", "fire"));
        DATA_LIST.push(Map.of("icon", "🐛", "code", ":bug:", "name", "bug"));
        DATA_LIST.push(Map.of("icon", "🚑️", "code", ":ambulance:", "name", "ambulance"));
        DATA_LIST.push(Map.of("icon", "✨", "code", ":sparkles:", "name", "sparkles"));
        DATA_LIST.push(Map.of("icon", "📝", "code", ":memo:", "name", "memo"));
        DATA_LIST.push(Map.of("icon", "🚀", "code", ":rocket:", "name", "rocket"));
        DATA_LIST.push(Map.of("icon", "💄", "code", ":lipstick:", "name", "lipstick"));
        DATA_LIST.push(Map.of("icon", "🎉", "code", ":tada:", "name", "tada"));
        DATA_LIST.push(Map.of("icon", "✅", "code", ":white_check_mark:", "name", "white-check-mark"));
        DATA_LIST.push(Map.of("icon", "🔒️", "code", ":lock:", "name", "lock"));
        DATA_LIST.push(Map.of("icon", "🔐", "code", ":closed_lock_with_key:", "name", "closed-lock-with-key"));
        DATA_LIST.push(Map.of("icon", "🔖", "code", ":bookmark:", "name", "bookmark"));
        DATA_LIST.push(Map.of("icon", "🚨", "code", ":rotating_light:", "name", "rotating-light"));
        DATA_LIST.push(Map.of("icon", "🚧", "code", ":construction:", "name", "construction"));
        DATA_LIST.push(Map.of("icon", "💚", "code", ":green_heart:", "name", "green-heart"));
        DATA_LIST.push(Map.of("icon", "⬇️", "code", ":arrow_down:", "name", "arrow-down"));
        DATA_LIST.push(Map.of("icon", "⬆️", "code", ":arrow_up:", "name", "arrow-up"));
        DATA_LIST.push(Map.of("icon", "📌", "code", ":pushpin:", "name", "pushpin"));
        DATA_LIST.push(Map.of("icon", "👷", "code", ":construction_worker:", "name", "construction-worker"));
        DATA_LIST.push(Map.of("icon", "📈", "code", ":chart_with_upwards_trend:", "name", "chart-with-upwards-trend"));
        DATA_LIST.push(Map.of("icon", "♻️", "code", ":recycle:", "name", "recycle"));
        DATA_LIST.push(Map.of("icon", "➕", "code", ":heavy_plus_sign:", "name", "heavy-plus-sign"));
        DATA_LIST.push(Map.of("icon", "➖", "code", ":heavy_minus_sign:", "name", "heavy-minus-sign"));
        DATA_LIST.push(Map.of("icon", "🔧", "code", ":wrench:", "name", "wrench"));
        DATA_LIST.push(Map.of("icon", "🔨", "code", ":hammer:", "name", "hammer"));
        DATA_LIST.push(Map.of("icon", "🌐", "code", ":globe_with_meridians:", "name", "globe-with-meridians"));
        DATA_LIST.push(Map.of("icon", "✏️", "code", ":pencil2:", "name", "pencil2"));
        DATA_LIST.push(Map.of("icon", "💩", "code", ":poop:", "name", "poop"));
        DATA_LIST.push(Map.of("icon", "⏪️", "code", ":rewind:", "name", "rewind"));
        DATA_LIST.push(Map.of("icon", "🔀", "code", ":twisted_rightwards_arrows:", "name", "twisted-rightwards-arrows"));
        DATA_LIST.push(Map.of("icon", "📦️", "code", ":package:", "name", "package"));
        DATA_LIST.push(Map.of("icon", "👽️", "code", ":alien:", "name", "alien"));
        DATA_LIST.push(Map.of("icon", "🚚", "code", ":truck:", "name", "truck"));
        DATA_LIST.push(Map.of("icon", "📄", "code", ":page_facing_up:", "name", "page-facing-up"));
        DATA_LIST.push(Map.of("icon", "💥", "code", ":boom:", "name", "boom"));
        DATA_LIST.push(Map.of("icon", "🍱", "code", ":bento:", "name", "bento"));
        DATA_LIST.push(Map.of("icon", "♿️", "code", ":wheelchair:", "name", "wheelchair"));
        DATA_LIST.push(Map.of("icon", "💡", "code", ":bulb:", "name", "bulb"));
        DATA_LIST.push(Map.of("icon", "🍻", "code", ":beers:", "name", "beers"));
        DATA_LIST.push(Map.of("icon", "💬", "code", ":speech_balloon:", "name", "speech-balloon"));
        DATA_LIST.push(Map.of("icon", "🗃️", "code", ":card_file_box:", "name", "card-file-box"));
        DATA_LIST.push(Map.of("icon", "🔊", "code", ":loud_sound:", "name", "loud-sound"));
        DATA_LIST.push(Map.of("icon", "🔇", "code", ":mute:", "name", "mute"));
        DATA_LIST.push(Map.of("icon", "👥", "code", ":busts_in_silhouette:", "name", "busts-in-silhouette"));
        DATA_LIST.push(Map.of("icon", "🚸", "code", ":children_crossing:", "name", "children-crossing"));
        DATA_LIST.push(Map.of("icon", "🏗️", "code", ":building_construction:", "name", "building-construction"));
        DATA_LIST.push(Map.of("icon", "📱", "code", ":iphone:", "name", "iphone"));
        DATA_LIST.push(Map.of("icon", "🤡", "code", ":clown_face:", "name", "clown-face"));
        DATA_LIST.push(Map.of("icon", "🥚", "code", ":egg:", "name", "egg"));
        DATA_LIST.push(Map.of("icon", "🙈", "code", ":see_no_evil:", "name", "see-no-evil"));
        DATA_LIST.push(Map.of("icon", "📸", "code", ":camera_flash:", "name", "camera-flash"));
        DATA_LIST.push(Map.of("icon", "⚗️", "code", ":alembic:", "name", "alembic"));
        DATA_LIST.push(Map.of("icon", "🔍️", "code", ":mag:", "name", "mag"));
        DATA_LIST.push(Map.of("icon", "🏷️", "code", ":label:", "name", "label"));
        DATA_LIST.push(Map.of("icon", "🌱", "code", ":seedling:", "name", "seedling"));
        DATA_LIST.push(Map.of("icon", "🚩", "code", ":triangular_flag_on_post:", "name", "triangular-flag-on-post"));
        DATA_LIST.push(Map.of("icon", "🥅", "code", ":goal_net:", "name", "goal-net"));
        DATA_LIST.push(Map.of("icon", "💫", "code", ":dizzy:", "name", "dizzy"));
        DATA_LIST.push(Map.of("icon", "🗑️", "code", ":wastebasket:", "name", "wastebasket"));
        DATA_LIST.push(Map.of("icon", "🛂", "code", ":passport_control:", "name", "passport-control"));
        DATA_LIST.push(Map.of("icon", "🩹", "code", ":adhesive_bandage:", "name", "adhesive-bandage"));
        DATA_LIST.push(Map.of("icon", "🧐", "code", ":monocle_face:", "name", "monocle-face"));
        DATA_LIST.push(Map.of("icon", "⚰️", "code", ":coffin:", "name", "coffin"));
        DATA_LIST.push(Map.of("icon", "🧪", "code", ":test_tube:", "name", "test-tube"));
        DATA_LIST.push(Map.of("icon", "👔", "code", ":necktie:", "name", "necktie"));
        DATA_LIST.push(Map.of("icon", "🩺", "code", ":stethoscope:", "name", "stethoscope"));
        DATA_LIST.push(Map.of("icon", "🧱", "code", ":bricks:", "name", "bricks"));
        DATA_LIST.push(Map.of("icon", "🧑‍💻", "code", ":technologist:", "name", "technologist"));
        DATA_LIST.push(Map.of("icon", "💸", "code", ":money_with_wings:", "name", "money-with-wings"));
        DATA_LIST.push(Map.of("icon", "🧵", "code", ":thread:", "name", "thread"));
        DATA_LIST.push(Map.of("icon", "🦺", "code", ":safety_vest:", "name", "safety-vest"));
    }

    /**
     * 获取 Gitmoji 数据列表 <br/>
     * icon：图标，code：Gitmoji代码
     */
    public static LinkedList<Map<String, String>> getDataList() {
        return DATA_LIST;
    }
}
