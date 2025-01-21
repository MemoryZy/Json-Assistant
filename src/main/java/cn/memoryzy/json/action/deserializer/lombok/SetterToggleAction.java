package cn.memoryzy.json.action.deserializer.lombok;

import cn.memoryzy.json.service.persistent.state.DeserializerState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/1/21
 */
public class SetterToggleAction extends ToggleAction {

    private final DeserializerState deserializerState;

    public SetterToggleAction(DeserializerState deserializerState) {
        super("@Setter", null, null);
        this.deserializerState = deserializerState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializerState.setterLombokAnnotation;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializerState.setterLombokAnnotation = state;
    }
}
