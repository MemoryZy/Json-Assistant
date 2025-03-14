package cn.memoryzy.json.enums;

import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.Urls;

import java.util.Objects;

/**
 * @author Memory
 * @since 2024/10/25
 */
public enum UrlType {

    /**
     * 默认（站点主页）
     */
    DEFAULT(JsonAssistantPlugin.PLUGIN_ID_NAME + ".#DEFAULT", Urls.OVERVIEW),

    /**
     * 分享链接（插件市场）
     */
    SHARE(JsonAssistantPlugin.PLUGIN_ID_NAME + ".#SHARE", Urls.MARKETPLACE_LINK),

    /**
     * 捐赠链接
     */
    DONATE(JsonAssistantPlugin.PLUGIN_ID_NAME + ".#DONATE", Urls.DONATE_LINK),

    /**
     * 邮件链接
     */
    MAIL(JsonAssistantPlugin.PLUGIN_ID_NAME + ".#MAIL", "mailto:" + Urls.EMAIL_LINK),

    /**
     * 关于 JSON 窗口相关介绍的页面
     */
    VIEW(JsonAssistantPlugin.PLUGIN_ID_NAME + ".#VIEW", Urls.VIEW),

    /**
     * 关于 JSON 树相关介绍的页面
     */
    SITE_TREE(JsonAssistantPlugin.PLUGIN_ID_NAME + ".#SITE_TREE", Urls.TREE_LINK),

    /**
     * 关于 JSON 反序列化相关介绍的页面
     */
    SITE_TO_JAVA_BEAN(JsonAssistantPlugin.PLUGIN_ID_NAME + ".#SITE_TO_JAVA_BEAN", Urls.JSON_TO_JAVA_BEAN_LINK),

    /**
     * 识别其他格式相关介绍
     */
    RECOGNIZE(JsonAssistantPlugin.PLUGIN_ID_NAME + ".#RECOGNIZE", Urls.RECOGNIZE),

    /**
     * 关于历史记录相关介绍的页面
     */
    HISTORY(JsonAssistantPlugin.PLUGIN_ID_NAME + ".#HISTORY", Urls.HISTORY),

    /**
     * 关于 YAML 弹窗相关介绍的页面
     */
    YAML_DIALOG(JsonAssistantPlugin.PLUGIN_ID_NAME + ".#YAML_DIALOG", Urls.YAML_DIALOG),

    ;

    private final String id;
    private final String url;

    UrlType(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public static UrlType of(String id) {
        for (UrlType value : values()) {
            if (Objects.equals(value.id, id)) {
                return value;
            }
        }

        return DEFAULT;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
}
