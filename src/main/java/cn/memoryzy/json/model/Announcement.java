package cn.memoryzy.json.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Memory
 * @since 2025/5/27
 */
public class Announcement {

    /**
     * 唯一标识（必填）
     */
    private String id;

    /**
     * 国际化
     */
    private Map<String, LocalizedNotice> locales;

    /**
     * 公告类型（info/warning/error）
     */
    private NoticeType type;

    /**
     * 显示优先级（数值越大越优先）
     */
    private Integer priority;

    /**
     * 生效日期（ISO8601）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date effectiveDate;

    /**
     * 过期日期（自动隐藏）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expirationDate;

    /**
     * 版本约束（语义化版本范围）
     */
    private String versionConstraints;

    /**
     * 展示次数
     */
    private Integer display;

    /**
     * 关联操作按钮
     */
    private List<NoticeAction> actions;

    /**
     * 扩展元数据
     */
    @JsonProperty("metadata")
    private NoticeMetadata noticeMetadata;


    public static class LocalizedNotice {

        /**
         * 标题
         */
        private String title;

        /**
         * 内容
         */
        private String content;


        // region Getter/Setter
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
        // endregion
    }

    public static class NoticeAction {

        private String label;

        // TODO URL的话也分locale
        private String url;

        /**
         * 指令
         * <pre>
         *     1.update    - 提示用户应该更新插件
         *     2.notPrompt - 直接关闭通知，并且显示次数直接到最高
         * </pre>
         */
        private String command;


        /*

        [
  {
    "id": "tort_v1",
    "locales": {
      "en_US": {
        "title": "Important Update",
        "content": "Added new features..."
      },
      "zh_CN": {
        "title": "Json Assistant",
        "content": "关于 Json Assistant 被恶意剽窃及二次分发的说明"
      }
    },
    "type": "info",
    "priority": 1,
    "effectiveDate": "2025-05-27",
    "expirationDate": "2025-06-30",
    "versionConstraints": ">=1.8.0",
    "display": 2,
    "actions": [
      {
        "label": "了解更多",
        "url": "https://xxxxxx"
      },
      {
        "label": "不再提示",
        "command": "notPrompt"
      }
    ],
    "metadata": {
      "author": "Memory",
      "createdAt": "2025-05-27"
    }
  }
]

         */

        // region Getter/Setter
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        // endregion
    }

    public static class NoticeMetadata {

        private String author;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date createdAt;


        // region Getter/Setter
        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }
        // endregion
    }

    public enum NoticeType {
        @JsonProperty("info") INFO,
        @JsonProperty("warning") WARNING,
        @JsonProperty("error") ERROR;
    }


    // region Getter/Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, LocalizedNotice> getLocales() {
        return locales;
    }

    public void setLocales(Map<String, LocalizedNotice> locales) {
        this.locales = locales;
    }

    public NoticeType getType() {
        return type;
    }

    public void setType(NoticeType type) {
        this.type = type;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getVersionConstraints() {
        return versionConstraints;
    }

    public void setVersionConstraints(String versionConstraints) {
        this.versionConstraints = versionConstraints;
    }

    public Integer getDisplay() {
        return display;
    }

    public void setDisplay(Integer display) {
        this.display = display;
    }

    public List<NoticeAction> getActions() {
        return actions;
    }

    public void setActions(List<NoticeAction> actions) {
        this.actions = actions;
    }

    public NoticeMetadata getNoticeMetadata() {
        return noticeMetadata;
    }

    public void setNoticeMetadata(NoticeMetadata noticeMetadata) {
        this.noticeMetadata = noticeMetadata;
    }
    // endregion
}