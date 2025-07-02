package cn.memoryzy.json.event;

import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/7/2
 */
public interface HistoryAddedEvent {

    Topic<HistoryAddedEvent> TOPIC = Topic.create("History Added", HistoryAddedEvent.class);

    void added();

}
