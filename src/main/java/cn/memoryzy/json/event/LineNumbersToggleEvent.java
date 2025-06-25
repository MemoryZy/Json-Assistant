package cn.memoryzy.json.event;

import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/6/25
 */
public interface LineNumbersToggleEvent {

    Topic<LineNumbersToggleEvent> TOPIC =
            Topic.create("Line Numbers Toggle", LineNumbersToggleEvent.class);

    void toggle(boolean display);

}
