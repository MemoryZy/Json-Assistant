package cn.memoryzy.json.event;

import com.intellij.openapi.editor.Editor;
import com.intellij.util.messages.Topic;

/**
 * @author Memory
 * @since 2025/6/23
 */
public interface RefreshFloatToolbarEvent {

    Topic<RefreshFloatToolbarEvent> ON_REFRESH_FLOAT_TOOLBAR = Topic.create("Refresh Float Toolbar", RefreshFloatToolbarEvent.class);

    void accept(Editor editor);

}
