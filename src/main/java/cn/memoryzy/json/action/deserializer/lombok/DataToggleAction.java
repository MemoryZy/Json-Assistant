package cn.memoryzy.json.action.deserializer.lombok;

import cn.memoryzy.json.service.persistent.state.DeserializationState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/1/21
 */
public class DataToggleAction extends ToggleAction implements UpdateInBackground {

    private final DeserializationState deserializationState;

    public DataToggleAction(DeserializationState deserializationState) {
        super("@Data", null, null);
        this.deserializationState = deserializationState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializationState.isEnableLombokData();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializationState.setEnableLombokData(state);
    }
}
