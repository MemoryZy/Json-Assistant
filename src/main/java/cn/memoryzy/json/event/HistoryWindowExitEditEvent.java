package cn.memoryzy.json.event;

import com.intellij.openapi.editor.Editor;
import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/7/11
 */
public interface HistoryWindowExitEditEvent {

    Topic<HistoryWindowExitEditEvent> TOPIC = Topic.create("History Window Exit Edit", HistoryWindowExitEditEvent.class);

    void handle(Editor editor);

}
