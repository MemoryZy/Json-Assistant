package cn.memoryzy.json.event;

import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/6/24
 */
public interface RegisterClipboardUsageEvent {

    Topic<RegisterClipboardUsageEvent> ON_REGISTER_CLIPBOARD_USAGE =
            Topic.create("Register Clipboard Usage", RegisterClipboardUsageEvent.class);

    void accept(String hash);

}
