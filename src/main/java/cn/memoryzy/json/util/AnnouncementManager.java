package cn.memoryzy.json.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.model.Announcement;
import cn.memoryzy.json.model.AnnouncementStats;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.notification.NotificationType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Memory
 * @since 2025/5/27
 */
public class AnnouncementManager {

    public static void showAnnouncement() {
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
        List<Notifications.FullContentNotification> notifications = announcements.stream()
                .map(el -> convertNotification(el, isChineseLocale))
                .collect(Collectors.toList());


    }

    private static Notifications.FullContentNotification convertNotification(Announcement announcement, boolean isChineseLocale) {
        // TODO 待完成



        return new Notifications.FullContentNotification(
                Notifications.getBalloonLogNotificationGroup().getDisplayId(),
                "",
                "",
                NotificationType.INFORMATION,
                announcement.getId()
        );
    }

    private static void filterAnnouncements(List<Announcement> announcements) {
        // 当前时间
        LocalDate now = LocalDate.now();
        // 已读公告
        Map<String, AnnouncementStats> announcementStatsMap = JsonAssistantPersistentState.getInstance().announcementStatsMap;
        // 当前插件版本
        String version = JsonAssistantPlugin.getVersion();

        // 过滤以下公告
        announcements.removeIf(el ->
                Objects.isNull(el.getId())
                        // 已展示过，并且展示次数超出设定值
                        || isExceedDisplayLimit(el, announcementStatsMap)
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
    public static boolean isExceedDisplayLimit(Announcement announcement, Map<String, AnnouncementStats> announcementStatsMap) {
        // 公告唯一标识
        String id = announcement.getId();
        // 展示次数
        Integer display = announcement.getDisplay();

        AnnouncementStats announcementStats = announcementStatsMap.get(id);
        if (null == announcementStats) {
            // 不存在说明没展示过，不过滤
            return false;
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
    public static boolean isNoticeExpired(Announcement announcement, LocalDate now) {
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
    public static boolean isVersionMismatched(Announcement announcement, String currentVersion) {
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
    public static boolean isNoticeNotEffective(Announcement announcement, LocalDate now) {
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
    public static List<Announcement> fetchAnnouncements(boolean isChineseLocale) {
        String url = isChineseLocale
                ? Urls.ANNOUNCEMENTS_SOURCE_GITEE_LINK
                : Urls.ANNOUNCEMENTS_SOURCE_GITHUB_LINK;

        try {
            // 拉取公告
            String respJson = HttpUtil.get(url, StandardCharsets.UTF_8);
            // 解析
            return JsonUtil.MAPPER.readValue(respJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

}
