package cn.memoryzy.json.event;

import cn.memoryzy.json.model.event.EditorCountCharEvent;
import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/8/26
 */
public interface EditorCharChangedEvent {

    Topic<EditorCharChangedEvent> TOPIC =
            Topic.create("Editor Char Changed", EditorCharChangedEvent.class);

    void change(EditorCountCharEvent event);

}
