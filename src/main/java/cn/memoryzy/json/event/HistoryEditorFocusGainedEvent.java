package cn.memoryzy.json.event;

import com.intellij.openapi.editor.Editor;
import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/8/8
 */
public interface HistoryEditorFocusGainedEvent {

    Topic<HistoryEditorFocusGainedEvent> TOPIC = Topic.create("History Editor Focus Gained", HistoryEditorFocusGainedEvent.class);

    void focusGained(Editor editor);

}
