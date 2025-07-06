package cn.memoryzy.json.event;

import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/7/6
 */
public interface NavigateRecordEvent {

    Topic<NavigateRecordEvent> TOPIC = Topic.create("Navigate Record", NavigateRecordEvent.class);

    void navigate(Integer id, boolean shouldEdit);

}
