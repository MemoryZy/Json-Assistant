package cn.memoryzy.json.model;

import cn.hutool.core.bean.BeanUtil;
import cn.memoryzy.json.model.serializer.LocalDateTimeTypeHandler;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;
import cn.memoryzy.json.util.Json5Util;

import java.util.Map;

/**
 * @author Memory
 * @since 2025/5/27
 */
public class AnnouncementStats {

    /**
     * 展示次数
     */
    private int displayCount = 0;

    /**
     * 最后展示时间戳
     */
    private long lastShownTime = 0L;

    /**
     * 是否能够再显示
     */
    private boolean shouldShowAgain = true;

    public AnnouncementStats() {
    }

    /**
     * 转为 Json5（供 JsonSerializer 的 addObj 方法调用）
     *
     * @return Json5
     */
    public String toJson() {
        Map<String, Object> map = BeanUtil.beanToMap(this);
        return Json5Util.toJson5Str(map, Json5Util.COMPACT_JSON5.handleType(new LocalDateTimeTypeHandler()));
    }

    public static AnnouncementStats fromMap(ObjectWrapper wrapper) {
        return BeanUtil.toBean(wrapper, AnnouncementStats.class);
    }


    public int getDisplayCount() {
        return displayCount;
    }

    public AnnouncementStats setDisplayCount(int displayCount) {
        this.displayCount = displayCount;
        return this;
    }

    public long getLastShownTime() {
        return lastShownTime;
    }

    public AnnouncementStats setLastShownTime(long lastShownTime) {
        this.lastShownTime = lastShownTime;
        return this;
    }

    public boolean isShouldShowAgain() {
        return shouldShowAgain;
    }

    public AnnouncementStats setShouldShowAgain(boolean shouldShowAgain) {
        this.shouldShowAgain = shouldShowAgain;
        return this;
    }
}
