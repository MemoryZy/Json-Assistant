package cn.memoryzy.json.action.deserializer.lombok;

import cn.memoryzy.json.service.persistent.state.v2.DeserializationState;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Memory
 * @since 2025/1/21
 */
public class LombokGroup extends DefaultActionGroup implements UpdateInBackground {

    private final DeserializationState deserializationState;

    public LombokGroup(DeserializationState deserializationState) {
        super("Lombok Annotations", true);
        this.deserializationState = deserializationState;
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        List<AnAction> actions = new ArrayList<>();
        actions.add(new DataToggleAction(deserializationState));
        actions.add(new AccessorsToggleAction(deserializationState));
        actions.add(new GetterToggleAction(deserializationState));
        actions.add(new SetterToggleAction(deserializationState));
        return actions.toArray(new AnAction[0]);
    }

}
