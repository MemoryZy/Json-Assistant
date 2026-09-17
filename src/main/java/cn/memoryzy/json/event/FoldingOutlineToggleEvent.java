package cn.memoryzy.json.event;

import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/6/25
 */
public interface FoldingOutlineToggleEvent {

    Topic<FoldingOutlineToggleEvent> TOPIC =
            Topic.create("Folding Outline Toggle", FoldingOutlineToggleEvent.class);

    void toggle(boolean display);

}
