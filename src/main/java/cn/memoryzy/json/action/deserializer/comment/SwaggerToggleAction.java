package cn.memoryzy.json.action.deserializer.comment;

import cn.memoryzy.json.service.persistent.state.v2.DeserializationState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/4/1
 */
public class SwaggerToggleAction extends ToggleAction implements UpdateInBackground {

    private final DeserializationState deserializationState;

    public SwaggerToggleAction(DeserializationState deserializationState) {
        super("@ApiModelProperty (Swagger)", null, null);
        this.deserializationState = deserializationState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializationState.isEnableSwaggerAnnotation();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializationState.setEnableSwaggerAnnotation(state);
    }
}
