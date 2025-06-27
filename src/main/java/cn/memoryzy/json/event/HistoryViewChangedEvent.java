package cn.memoryzy.json.event;

import cn.memoryzy.json.enums.HistoryDisplayMode;
import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/6/27
 */
public interface HistoryViewChangedEvent {

    Topic<HistoryViewChangedEvent> TOPIC = Topic.create("History View Changed", HistoryViewChangedEvent.class);

    void change(HistoryDisplayMode mode);

}
