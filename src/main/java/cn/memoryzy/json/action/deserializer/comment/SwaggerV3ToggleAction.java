package cn.memoryzy.json.action.deserializer.comment;

import cn.memoryzy.json.service.persistent.state.DeserializationState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/4/1
 */
public class SwaggerV3ToggleAction extends ToggleAction implements UpdateInBackground {

    private final DeserializationState deserializationState;

    public SwaggerV3ToggleAction(DeserializationState deserializationState) {
        super("@Schema (Swagger V3)", null, null);
        this.deserializationState = deserializationState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializationState.isEnableSwagger3Annotation();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializationState.setEnableSwagger3Annotation(state);
    }

}
