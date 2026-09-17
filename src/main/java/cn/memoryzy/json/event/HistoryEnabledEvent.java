package cn.memoryzy.json.event;

import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/6/27
 */
public interface HistoryEnabledEvent {

    Topic<HistoryEnabledEvent> TOPIC = Topic.create("History Enabled", HistoryEnabledEvent.class);

    void enable(boolean enable);

}
