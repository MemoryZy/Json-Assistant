package cn.memoryzy.json.action.deserializer.lombok;

import cn.memoryzy.json.service.persistent.state.DeserializerState;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2025/1/21
 */
public class LombokGroup extends DefaultActionGroup {

    private final DeserializerState deserializerState;

    public LombokGroup(DeserializerState deserializerState) {
        super("Lombok Annotations", true);
        this.deserializerState = deserializerState;
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        List<AnAction> actions = new ArrayList<>();
        actions.add(new DataToggleAction(deserializerState));
        actions.add(new AccessorsToggleAction(deserializerState));
        actions.add(new GetterToggleAction(deserializerState));
        actions.add(new SetterToggleAction(deserializerState));
        return actions.toArray(new AnAction[0]);
    }

}
