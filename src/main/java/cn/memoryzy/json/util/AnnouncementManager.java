package cn.memoryzy.json.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.http.HttpUtil;
import cn.memoryzy.json.constant.JsonAssistantPlugin;
import cn.memoryzy.json.constant.Urls;
import cn.memoryzy.json.model.Announcement;
import cn.memoryzy.json.model.AnnouncementStats;
import cn.memoryzy.json.service.persistent.JsonAssistantPersistentState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Memory
 * @since 2025/5/27
 */
public class AnnouncementManager {

    public static void showAnnouncement() {
        // 拉取
        List<Announcement> announcements = fetchAnnouncements();
        if (CollUtil.isEmpty(announcements)) return;

        // 过滤
        filterAnnouncements(announcements);


    }

    private static void filterAnnouncements(List<Announcement> announcements) {
        // 过滤：
        //  1. 已展示过，并且展示次数超出设定值
        //  2. 已过期
        //  3. 版本约束
        //  4. 没到生效日期

        // 当前时间
        LocalDate now = LocalDate.now();
        // 已读公告
        Map<String, AnnouncementStats> announcementStatsMap = JsonAssistantPersistentState.getInstance().announcementStatsMap;
        // 当前插件版本
        String version = JsonAssistantPlugin.getVersion();
        // 过滤
        announcements.removeIf(el ->
                isExceedDisplayLimit(el, announcementStatsMap)
                        || isNoticeExpired(el, now)


        );
    }

    /**
     * 检查公告是否因展示超限需过滤
     *
     * @return 超出次数返回 true
     */
    public static boolean isExceedDisplayLimit(Announcement announcement, Map<String, AnnouncementStats> announcementStatsMap) {

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
     * @param notice         公告对象
     * @param currentVersion 当前插件版本
     * @return 版本不匹配返回true
     */
    public boolean isVersionMismatched(PluginNotice notice, String currentVersion) {
        return !VersionChecker.match(currentVersion, notice.getVersionConstraints());
    }

    /**
     * 检查公告是否尚未生效
     *
     * @return 当前时间早于生效日期返回true
     */
    public boolean isNoticeNotEffective(Announcement announcement, LocalDate now) {
        Date effectiveDate = announcement.getEffectiveDate();
        // 若生效日期为空，则立即生效
        if (null == effectiveDate) {

        }

        return LocalDate.now().isBefore(announcement.getEffectiveDate());
    }

    public static List<Announcement> fetchAnnouncements() {
        // 拉取公告
        String respJson = HttpUtil.get(Urls.ANNOUNCEMENTS_SOURCE_GITEE_LINK, StandardCharsets.UTF_8);

        try {
            // 解析
            return JsonUtil.MAPPER.readValue(respJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }


}
