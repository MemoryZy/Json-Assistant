package cn.memoryzy.json.event;

import cn.memoryzy.json.enums.HistoryAffectType;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.Nullable;

/**
 * @author Memory
 * @since 2025/9/19
 */
public interface RefreshHistoryTreeEvent {

    Topic<RefreshHistoryTreeEvent> TOPIC = Topic.create("Refresh History Tree", RefreshHistoryTreeEvent.class);

    void refresh(Project sourceProject, HistoryAffectType affectType, @Nullable String newNodeId);

}
