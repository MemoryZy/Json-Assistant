package cn.memoryzy.json.service.persistent.state;

import com.intellij.util.xmlb.annotations.Tag;

/**
 * @author Memory
 * @since 2025/5/27
 */
@Tag("stats")
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
