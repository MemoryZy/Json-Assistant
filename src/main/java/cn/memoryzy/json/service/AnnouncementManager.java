package cn.memoryzy.json.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.memoryzy.json.JsonAssistantPlugin;
import cn.memoryzy.json.constant.PluginConstant;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.model.deserializer.Announcement;
import cn.memoryzy.json.service.persistent.GeneralSettings;
import cn.memoryzy.json.service.persistent.state.AnnouncementStats;
import cn.memoryzy.json.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.plugins.PluginManagerConfigurable;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.Alarm;
import com.intellij.util.AlarmFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/5/27
 */
@Service(Service.Level.APP)
public final class AnnouncementManager implements Disposable {

    private static final Logger LOG = Logger.getInstance(AnnouncementManager.class);

    public static AnnouncementManager getInstance() {
        return ApplicationManager.getApplication().getService(AnnouncementManager.class);
    }

    private final Alarm alarm = AlarmFactory.getInstance().create(Alarm.ThreadToUse.POOLED_THREAD, this);

    /**
     * 延迟执行时间（分钟）
     */
    private static final int delayMinutes = 2;

    public void scheduleDelayedAnnouncement(@NotNull Project project) {
        if (alarm.isDisposed() || project.isDisposed()) return;

        alarm.addRequest(() -> {
            if (!project.isDisposed()) {
                showAnnouncement(project);
            }
        }, JsonAssistantUtil.minutesToMilliseconds(delayMinutes));
    }

    private void showAnnouncement(@NotNull Project project) {
        NotificationScheduler scheduler = NotificationScheduler.getInstance();

        try {
            // 是否为中国区域
            boolean isChineseLocale = PlatformUtil.isChineseLocale();

            // 拉取公告
            List<Announcement> announcements = fetchAnnouncements(isChineseLocale);

            // 过滤
            filterAnnouncements(announcements);

            // 过滤
            if (CollUtil.isEmpty(announcements)) return;

            // 按优先级排序
            announcements.sort(Comparator.comparing(Announcement::getPriority, Comparator.nullsLast(Comparator.reverseOrder())));

            // 转换为通知
            List<Notification> notifications = announcements.stream()
                    .map(el -> convertNotification(project, el, isChineseLocale))
                    .collect(Collectors.toList());

            // 计划显示通知
            scheduler.addNotifications(notifications, this::addReadNoticeRecord, project);

        } catch (Exception e) {
            LOG.warn("[Json Assistant] The announcement shows an error.", e);
        } finally {
            Disposer.dispose(alarm);
            scheduler.disposeAlarm();
        }
    }

    private Notification convertNotification(@NotNull Project project, Announcement announcement, boolean isChineseLocale) {
        // 目前只做中英双语
        Map<String, Announcement.LocaleContent> localeMap = announcement.getLocales();

        Announcement.LocaleContent localeContent;
        if (localeMap.size() == 1) {
            localeContent = localeMap.values().iterator().next();
        } else {
            Announcement.LocaleContent cnLocalizedNotice = localeMap.get(PluginConstant.zh_CN);
            Announcement.LocaleContent usLocalizedNotice = localeMap.get(PluginConstant.en_US);
            localeContent = isChineseLocale ? cnLocalizedNotice : usLocalizedNotice;
        }

        return getFullContentNotification(project, announcement, localeContent);
    }

    @NotNull
    @SuppressWarnings("deprecation")
    private Notification getFullContentNotification(@NotNull Project project, Announcement announcement, Announcement.LocaleContent localizedNotice) {
        String title = localizedNotice.getTitle();
        String content = localizedNotice.getContent();
        content = JsonAssistantUtil.wrapBody(content);
        Boolean autoHide = announcement.getAutoHide();

        String displayId = Boolean.FALSE.equals(autoHide)
                // 不自动消失
                ? Notifications.getStickyLogNotificationGroup().getDisplayId()
                // 自动消失（默认）
                : Notifications.getBalloonLogNotificationGroup().getDisplayId();

        // 默认 info
        NotificationType notificationType = NotificationType.INFORMATION;
        switch (announcement.getType()) {
            case WARNING:
                notificationType = NotificationType.WARNING;
                break;
            case ERROR:
                notificationType = NotificationType.ERROR;
                break;
        }

        // Action转换
        List<AnAction> actions = collectActions(project, announcement, localizedNotice.getActions());

        Notifications.FullContentNotification notification = new Notifications.FullContentNotification(
                displayId,
                title,
                content,
                notificationType,
                announcement.getId());

        actions.forEach(notification::addAction);

        // 注册网址监听
        notification.setListener(new Notifications.NotificationListenerImpl());

        return notification;
    }

    private List<AnAction> collectActions(@NotNull Project project, Announcement announcement, List<Announcement.NoticeAction> noticeActions) {
        List<AnAction> actions = new ArrayList<>();
        if (CollUtil.isNotEmpty(noticeActions)) {
            for (Announcement.NoticeAction noticeAction : noticeActions) {
                String label = noticeAction.getLabel();
                String url = noticeAction.getUrl();
                Announcement.CommandType command = noticeAction.getCommand();
                if (StrUtil.isBlank(label)) continue;

                AnAction action = null;
                if (StrUtil.isNotBlank(url)) {
                    action = createUrlAction(project, label, url, command, announcement);

                } else if (Objects.nonNull(command) && Announcement.CommandType.UNKNOWN != command) {
                    action = createCommandAction(project, label, command, announcement);
                }

                if (null != action) actions.add(action);
            }
        }

        return actions;
    }

    /**
     * 处理 URL 跳转
     */
    private AnAction createUrlAction(@NotNull Project project, String label, String url, Announcement.CommandType command, Announcement announcement) {
        Runnable call;
        if (Objects.nonNull(command) && Announcement.CommandType.UNKNOWN != command) {
            Runnable commandAction = createCommandAction(project, command, announcement);
            if (null == commandAction) {
                call = () -> BrowserUtil.browse(url);

            } else {
                call = () -> {
                    commandAction.run();
                    BrowserUtil.browse(url);
                };
            }

        } else {
            call = () -> BrowserUtil.browse(url);
        }

        return NotificationAction.createSimpleExpiring(label, call);
    }

    private AnAction createCommandAction(@NotNull Project project, String label, Announcement.CommandType command, Announcement announcement) {
        Runnable commandAction = createCommandAction(project, command, announcement);
        if (null == commandAction) return null;
        return NotificationAction.createSimpleExpiring(label, commandAction);
    }

    private Runnable createCommandAction(@NotNull Project project, Announcement.CommandType command, Announcement announcement) {
        switch (command) {
            case UPDATE: {
                // 打开插件页面，选中 Json Assistant 插件
                return () -> ShowSettingsUtilImpl.showSettingsDialog(project, PluginManagerConfigurable.ID, JsonAssistantPlugin.PLUGIN_NAME);
            }

            case NOT_PROMPT: {
                return () -> {
                    // 此公告不再显示，加上标记
                    String id = announcement.getId();
                    // 获取此公告记录
                    Map<String, AnnouncementStats> readAnnouncements = GeneralSettings.getInstance().getState().getReadAnnouncements();
                    // 若不存在此公告记录，则建立新的填充进去
                    AnnouncementStats announcementStats = readAnnouncements.computeIfAbsent(id, k -> new AnnouncementStats());
                    // 设置 [不再显示]
                    announcementStats.setShouldShowAgain(false);
                };
            }
        }

        return null;
    }

    /**
     * 添加用户已读的公告记录
     *
     * @param announcementId 公告ID
     */
    private void addReadNoticeRecord(String announcementId) {
        // 添加已读记录
        if (StrUtil.isBlank(announcementId)) return;

        Map<String, AnnouncementStats> readAnnouncements = GeneralSettings.getInstance().getState().getReadAnnouncements();
        AnnouncementStats announcementStats = readAnnouncements.computeIfAbsent(announcementId, k -> new AnnouncementStats());

        // 展示次数 +1
        announcementStats
                .setDisplayCount(announcementStats.getDisplayCount() + 1)
                .setLastShownTime(System.currentTimeMillis());
    }

    private void filterAnnouncements(List<Announcement> announcements) {
        // 当前时间
        LocalDate now = LocalDate.now();
        // 已读公告
        Map<String, AnnouncementStats> readAnnouncements = GeneralSettings.getInstance().getState().getReadAnnouncements();
        // 当前插件版本
        String version = JsonAssistantPlugin.getVersion();

        // 过滤以下公告
        announcements.removeIf(el ->
                Objects.isNull(el.getId())
                        // 标题内容不存在
                        || MapUtil.isEmpty(el.getLocales())
                        // 已展示过，并且展示次数超出设定值
                        || isExceedDisplayLimit(el, readAnnouncements)
                        // 公告过期
                        || isNoticeExpired(el, now)
                        // 版本约束
                        || isVersionMismatched(el, version)
                        // 未到生效时间
                        || isNoticeNotEffective(el, now));
    }

    /**
     * 检查公告是否因展示超限需过滤
     *
     * @return 超出次数返回 true
     */
    private boolean isExceedDisplayLimit(Announcement announcement, Map<String, AnnouncementStats> announcementStatsMap) {
        // 公告唯一标识
        String id = announcement.getId();
        // 展示次数
        Integer display = announcement.getDisplay();

        AnnouncementStats announcementStats = announcementStatsMap.get(id);
        if (null == announcementStats) {
            // 不存在说明没展示过，不过滤
            return false;
        }

        // 如果不能再显示，则直接返回 true 以被过滤
        if (!announcementStats.isShouldShowAgain()) {
            return true;
        }

        // 若今天已经展示过，则不再展示
        long lastShownTime = announcementStats.getLastShownTime();
        if (JsonAssistantUtil.isValidTimestamp(lastShownTime + "")) {
            LocalDate localDate = LocalDateTimeUtil.of(lastShownTime).toLocalDate();
            if (LocalDate.now().equals(localDate)) {
                return true;
            }
        }

        // 展示次数若为空，则默认1次
        int displayNum = null == display ? 1 : display;
        // 已展示的次数
        int displayCount = announcementStats.getDisplayCount();

        return displayCount >= displayNum;
    }

    /**
     * 检查公告是否已过期（基于expirationDate字段）
     *
     * @return 当前时间超过过期时间返回 true
     */
    private boolean isNoticeExpired(Announcement announcement, LocalDate now) {
        Date expirationDate = announcement.getExpirationDate();
        // 若过期时间为空，则永不过期
        if (null == expirationDate) {
            return false;
        }

        LocalDate expirationLocalDate = LocalDateTimeUtil.of(expirationDate).toLocalDate();
        return now.isAfter(expirationLocalDate);
    }

    /**
     * 检查当前版本是否符合公告的版本约束
     *
     * @param announcement   公告对象
     * @param currentVersion 当前插件版本
     * @return 版本不匹配返回true
     */
    private boolean isVersionMismatched(Announcement announcement, String currentVersion) {
        String versionConstraints = announcement.getVersionConstraints();
        if (StrUtil.isBlank(versionConstraints)) {
            return false;
        }

        return !VersionComparator.checkVersionConstraint(currentVersion, announcement.getVersionConstraints());
    }

    /**
     * 检查公告是否尚未生效
     *
     * @return 当前时间早于生效日期返回true
     */
    private boolean isNoticeNotEffective(Announcement announcement, LocalDate now) {
        Date effectiveDate = announcement.getEffectiveDate();
        // 若生效日期为空，则立即生效
        if (null == effectiveDate) {
            return false;
        }

        LocalDate expirationLocalDate = LocalDateTimeUtil.of(effectiveDate).toLocalDate();
        return now.isBefore(expirationLocalDate);
    }

    /**
     * 拉取公告内容 (JSON)
     */
    private List<Announcement> fetchAnnouncements(boolean isChineseLocale) {
        String url = isChineseLocale
                ? Urls.ANNOUNCEMENTS_SOURCE_GITEE_LINK
                : Urls.ANNOUNCEMENTS_SOURCE_GITHUB_LINK;

        try {
            // 拉取公告
            String respJson = HttpUtil.get(url, StandardCharsets.UTF_8);
            // 解析
            return JsonUtil.MAPPER.readValue(respJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void dispose() {

    }
}
