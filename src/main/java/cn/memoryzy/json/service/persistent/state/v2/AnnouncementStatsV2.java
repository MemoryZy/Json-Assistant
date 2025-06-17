package cn.memoryzy.json.service.persistent.state.v2;

import cn.hutool.core.bean.BeanUtil;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;

/**
 * @author Memory
 * @since 2025/5/27
 */
// @Tag("items")
public class AnnouncementStatsV2 {

    /**
     * 展示次数
     */
    public int displayCount = 0;

    /**
     * 最后展示时间戳
     */
    public long lastShownTime = 0L;

    /**
     * 是否能够再显示
     */
    public boolean shouldShowAgain = true;

    public AnnouncementStatsV2() {
    }

    public static AnnouncementStatsV2 fromMap(ObjectWrapper wrapper) {
        return BeanUtil.toBean(wrapper, AnnouncementStatsV2.class);
    }


    public int getDisplayCount() {
        return displayCount;
    }

    public AnnouncementStatsV2 setDisplayCount(int displayCount) {
        this.displayCount = displayCount;
        return this;
    }

    public long getLastShownTime() {
        return lastShownTime;
    }

    public AnnouncementStatsV2 setLastShownTime(long lastShownTime) {
        this.lastShownTime = lastShownTime;
        return this;
    }

    public boolean isShouldShowAgain() {
        return shouldShowAgain;
    }

    public AnnouncementStatsV2 setShouldShowAgain(boolean shouldShowAgain) {
        this.shouldShowAgain = shouldShowAgain;
        return this;
    }
}
