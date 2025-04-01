package cn.memoryzy.json.action.deserializer.comment;

import cn.memoryzy.json.service.persistent.state.DeserializerState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2025/4/1
 */
public class SwaggerToggleAction extends ToggleAction implements UpdateInBackground {

    private final DeserializerState deserializerState;

    public SwaggerToggleAction(DeserializerState deserializerState) {
        super("@ApiModelProperty (Swagger)", null, null);
        this.deserializerState = deserializerState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializerState.swaggerAnnotation;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializerState.swaggerAnnotation = state;
    }
}
