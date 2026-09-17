package cn.memoryzy.json.event;

import cn.memoryzy.json.enums.ColorScheme;
import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/6/25
 */
public interface ColorSchemeChangedEvent {

    Topic<ColorSchemeChangedEvent> TOPIC =
            Topic.create("Color Scheme Changed", ColorSchemeChangedEvent.class);

    void change(ColorScheme colorScheme);

}
