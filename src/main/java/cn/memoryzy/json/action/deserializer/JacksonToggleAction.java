package cn.memoryzy.json.action.deserializer;

import cn.memoryzy.json.service.persistent.state.DeserializationState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import org.jetbrains.annotations.NotNull;

/**
 * @author Memory
 * @since 2024/12/26
 */
public class JacksonToggleAction extends ToggleAction implements UpdateInBackground {

    private final DeserializationState deserializationState;

    public JacksonToggleAction(DeserializationState deserializationState) {
        super("@JsonProperty (Jackson)", null, null);
        this.deserializationState = deserializationState;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return deserializationState.isEnableJacksonAnnotation();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        deserializationState.setEnableJacksonAnnotation(state);
    }

}