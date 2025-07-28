package cn.memoryzy.json.event;

import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/7/24
 */
public interface ApplyToSourceToggleEvent {

    Topic<ApplyToSourceToggleEvent> TOPIC = Topic.create("Apply To Source Toggle", ApplyToSourceToggleEvent.class);

    void apply(boolean apply);

}
