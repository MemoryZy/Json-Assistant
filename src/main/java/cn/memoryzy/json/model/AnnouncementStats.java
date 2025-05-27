package cn.memoryzy.json.model;

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
    private Integer displayCount;

    /**
     * 最后展示时间戳
     */
    private Long lastShownTime;


    public static AnnouncementStats fromMap(ObjectWrapper wrapper) {
        return BeanUtil.toBean(wrapper, AnnouncementStats.class);
    }


    public int getDisplayCount() {
        return displayCount;
    }

    public void setDisplayCount(int displayCount) {
        this.displayCount = displayCount;
    }

    public long getLastShownTime() {
        return lastShownTime;
    }

    public void setLastShownTime(long lastShownTime) {
        this.lastShownTime = lastShownTime;
    }
}
