package cn.memoryzy.json.event;

import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/7/6
 */
public interface NavigateRecordEvent {

    Topic<NavigateRecordEvent> TOPIC = Topic.create("Navigate Record", NavigateRecordEvent.class);

    /**
     * 导航到具体的记录
     *
     * @param id         记录ID
     * @param needNaming 是否需要重命名
     */
    void navigate(String id, boolean needNaming);

}
