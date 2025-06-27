package cn.memoryzy.json.event;

import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/6/27
 */
public interface HistoryToggleEvent {

    Topic<HistoryToggleEvent> TOPIC = Topic.create("History Toggle", HistoryToggleEvent.class);

    void toggle(boolean enable);

}
