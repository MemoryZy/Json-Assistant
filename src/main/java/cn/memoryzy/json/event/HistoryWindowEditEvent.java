package cn.memoryzy.json.event;

import com.intellij.openapi.editor.Editor;
import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/7/11
 */
public interface HistoryWindowEditEvent {

    Topic<HistoryWindowEditEvent> TOPIC = Topic.create("History Window Edit", HistoryWindowEditEvent.class);

    void handle(Editor editor);

}
