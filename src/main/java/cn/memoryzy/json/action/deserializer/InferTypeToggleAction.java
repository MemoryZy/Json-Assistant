package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.bundle.JsonAssistantBundle;
import cn.memoryzy.json.service.persistent.state.DeserializationState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/7/25
 */
public class InferTypeToggleAction extends ToggleAction implements UpdateInBackground {

    private final DeserializationState deserializationState;

    public InferTypeToggleAction(DeserializationState deserializationState) {
        super(JsonAssistantBundle.messageOnSystem("action.deserialize.inferType.text"), null, null);
        this.deserializationState = deserializationState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializationState.isUseInferType();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializationState.setUseInferType(state);
    }
}
