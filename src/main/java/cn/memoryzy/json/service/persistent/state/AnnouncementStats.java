package cn.memoryzy.json.service.persistent.state;

import cn.hutool.core.bean.BeanUtil;
import cn.memoryzy.json.model.wrapper.ObjectWrapper;

/**
 * @author Memory
 * @since 2025/5/27
 */
public class AnnouncementStats {

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

    public AnnouncementStats() {
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
