package cn.memoryzy.json.event;

import com.intellij.openapi.editor.Editor;
import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/6/24
 */
public interface RegisterClipboardUsageEvent {

    Topic<RegisterClipboardUsageEvent> TOPIC = Topic.create("Register Clipboard Usage", RegisterClipboardUsageEvent.class);

    void accept(Editor editor, String hash);

}
